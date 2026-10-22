package com.oromist.entity;

import com.oromist.OromistNPC;
import com.oromist.entity.ai.OrcCombatController;
import com.oromist.entity.ai.OrcFlankGoal;
import com.oromist.entity.ai.OrcRetreatGoal;
import com.oromist.entity.ai.ShamanWarBloodGoal;
import com.oromist.entity.ai.WeaponCombatStrategy;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.nemesis.domain.OrcTrait;
import com.oromist.nemesis.persistence.NemesisCodecs;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * El orco. Combate con espada, hacha, arco, ballesta, lanza (montado en un caballo zombi)
 * o magia de chamán según su arquetipo, tiene una altura variable y puede estar vinculado a
 * un {@link OrcProfile} del sistema Némesis.
 *
 * <p>La entidad se limita a comportamiento y presentación; las decisiones de memoria,
 * ascensos y diálogos viven en la capa Némesis y se conectan mediante {@link OrcHooks}.</p>
 */
public class OrcEntity extends Monster implements CrossbowAttackMob {
	public static final int SKIN_VARIANTS = 3;

	private static final EntityDataAccessor<Boolean> CHARGING_CROSSBOW =
			SynchedEntityData.defineId(OrcEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> SKIN_VARIANT =
			SynchedEntityData.defineId(OrcEntity.class, EntityDataSerializers.INT);

	private static final Identifier RANK_MODIFIER = OromistNPC.id("rank");
	private static final Identifier SIZE_MODIFIER = OromistNPC.id("size");
	/**
	 * Distancia a la que, al perder de vista a su objetivo, el orco considera que el jugador huyó.
	 * Debe ser menor que FOLLOW_RANGE (35), porque a esa distancia la IA vanilla suelta al objetivo.
	 */
	private static final double FLEE_DISTANCE = 24.0;
	/** Duración mínima del combate (ticks) para que alejarse cuente como huida. */
	private static final int MIN_FIGHT_TICKS = 100;
	/** Por debajo de este porcentaje de vida un orco puede decidir huir. */
	private static final float RETREAT_HEALTH_RATIO = 0.25f;
	/** Por debajo de este porcentaje de vida un soldado raso se deja interrogar. */
	private static final float INTERROGATION_HEALTH_RATIO = 0.3f;
	/** Tiempo mínimo entre dos gritos de guerra del mismo orco (ticks). */
	private static final int WAR_CRY_COOLDOWN = 30 * 20;

	private static OrcHooks hooks = OrcHooks.NONE;

	private final OrcCombatController combat = new OrcCombatController(this, WeaponCombatStrategy.DEFAULTS);
	private final Set<OrcTrait> traits = EnumSet.noneOf(OrcTrait.class);
	private OrcArchetype archetype = OrcArchetype.SWORDSMAN;
	private OrcRank rankCache = OrcRank.GRUNT;
	private @Nullable UUID profileId;
	private int incarnation;
	private @Nullable OrcRank pendingRank;
	private boolean profileApplied;
	private boolean retreatDecided;
	private boolean retreating;
	private @Nullable UUID engagedPlayer;
	private int engagedTicks;
	/** Un capitán o caudillo cerca le da valor: golpea más fuerte y no huye. */
	private boolean inspired;
	private long lastWarCry = Long.MIN_VALUE;
	/** El jinete ya recibió su montura (aunque haya muerto, no se repone). */
	private boolean mountSummoned;
	/** Ya fue interrogado: no se puede volver a sacar información del mismo orco. */
	private boolean interrogated;

	public OrcEntity(EntityType<? extends OrcEntity> type, Level level) {
		super(type, level);
		if (getNavigation() instanceof GroundPathNavigation navigation) {
			navigation.setCanOpenDoors(true);
		}
		combat.reassess();
	}

	/** Lo llama el inicializador del mod para conectar la entidad con el sistema Némesis. */
	public static void installHooks(OrcHooks newHooks) {
		hooks = Objects.requireNonNull(newHooks);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 26.0)
				.add(Attributes.ATTACK_DAMAGE, 4.0)
				.add(Attributes.MOVEMENT_SPEED, 0.27)
				.add(Attributes.FOLLOW_RANGE, 35.0)
				.add(Attributes.ARMOR, 2.0);
	}

	// --- IA -------------------------------------------------------------------------

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new OrcRetreatGoal(this, 1.3));
		goalSelector.addGoal(2, new ShamanWarBloodGoal(this));
		goalSelector.addGoal(3, new OrcFlankGoal(this));
		// Prioridad 4: objetivo de combate según arma (OrcCombatController).
		goalSelector.addGoal(5, new OpenDoorGoal(this, true));
		goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.9));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));

		// Ignora el daño de otros orcos (fuego amigo) y avisa a los orcos cercanos al ser atacado.
		targetSelector.addGoal(1, new HurtByTargetGoal(this, OrcEntity.class).setAlertOthers());
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
		targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
	}

	/** Acceso para los controladores de IA del paquete {@code ai}. */
	public GoalSelector goalSelector() {
		return goalSelector;
	}

	@Override
	public void onEquipItem(EquipmentSlot slot, ItemStack oldStack, ItemStack stack) {
		super.onEquipItem(slot, oldStack, stack);
		// combat puede ser null si el constructor padre equipa algo antes de inicializar los campos.
		if (combat != null && !level().isClientSide()) {
			combat.reassess();
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (archetype.isMounted() && !mountSummoned) {
			mountSummoned = true;
			OrcMounts.summonFor(this, level);
		}
		trackEngagedPlayer();
		if (tickCount % 20 == 0) {
			TraitEffects.tickAll(this);
			updateInspiration();
			if (TraitEffects.isEnraged(this) && getTarget() instanceof ServerPlayer player) {
				hooks.onTraitShown(this, player, OrcTrait.BERSERKER);
			}
			hooks.onServerSecond(this);
		}
	}

	/** Solo los soldados rasos se inspiran; los líderes son la inspiración. */
	private void updateInspiration() {
		inspired = !currentRank().isNamed() && OrcSquad.hasLeaderNearby(this);
		OrcSquad.updateInspiration(this, inspired);
	}

	public boolean isInspired() {
		return inspired;
	}

	/**
	 * Detecta si el jugador con el que combatía huyó: la IA vanilla suelta al objetivo cuando
	 * se aleja o lo pierde de vista, así que se comprueba justo en ese momento.
	 */
	private void trackEngagedPlayer() {
		if (getTarget() instanceof ServerPlayer player && player.isAlive()) {
			if (!player.getUUID().equals(engagedPlayer)) {
				engagedPlayer = player.getUUID();
				engagedTicks = 0;
				onNewPrey(player);
			}
			engagedTicks++;
			return;
		}
		if (engagedPlayer != null && engagedTicks > MIN_FIGHT_TICKS && !retreating
				&& level().getPlayerByUUID(engagedPlayer) instanceof ServerPlayer escaped
				&& escaped.isAlive() && !escaped.isSpectator() && !escaped.isCreative()
				&& distanceTo(escaped) > FLEE_DISTANCE && getHealth() > getMaxHealth() * 0.5f) {
			hooks.onTargetFled(this, escaped);
		}
		engagedPlayer = null;
		engagedTicks = 0;
	}

	/** Empieza a cazar a un jugador: avisa a los suyos y deja ver su velocidad. */
	private void onNewPrey(ServerPlayer player) {
		long now = level().getGameTime();
		if (now - lastWarCry >= WAR_CRY_COOLDOWN) {
			lastWarCry = now;
			OrcSquad.warCry(this, player);
		}
		if (traits.contains(OrcTrait.SWIFT)) {
			hooks.onTraitShown(this, player, OrcTrait.SWIFT);
		}
	}

	/**
	 * Responde al grito de guerra de otro orco: se une a la caza, pero no grita a su vez
	 * (si no, una aldea entera rugiría en cadena).
	 */
	public void answerWarCry(LivingEntity prey) {
		lastWarCry = level().getGameTime();
		setTarget(prey);
	}

	// --- Huida ----------------------------------------------------------------------

	/**
	 * ¿Debe huir? Los cobardes siempre huyen al quedar malheridos; los orcos con nombre
	 * a veces también, para sobrevivir y vengarse. La decisión se toma una sola vez.
	 */
	public boolean wantsToRetreat() {
		if (retreating) {
			return true;
		}
		if (getHealth() > getMaxHealth() * RETREAT_HEALTH_RATIO) {
			return false;
		}
		if (!retreatDecided) {
			if (inspired) {
				return false; // Con su capitán mirando no se atreve; lo decidirá si el capitán cae.
			}
			retreatDecided = true;
			boolean named = currentRank().isNamed();
			retreating = traits.contains(OrcTrait.COWARD) || (named && getRandom().nextFloat() < 0.35f);
		}
		return retreating;
	}

	public void onStartRetreat(LivingEntity threat) {
		hooks.onRetreatStarted(this, threat);
		if (traits.contains(OrcTrait.COWARD) && threat instanceof ServerPlayer player) {
			hooks.onTraitShown(this, player, OrcTrait.COWARD);
		}
	}

	/** Huye ya, sin tirar dados (por ejemplo, tras ser interrogado). */
	public void forceRetreat() {
		retreatDecided = true;
		retreating = true;
	}

	// --- Interrogatorio ----------------------------------------------------------------

	/** ¿Está lo bastante malherido para que lo intenten interrogar? */
	public boolean isBroken() {
		return isAlive() && getHealth() <= getMaxHealth() * INTERROGATION_HEALTH_RATIO;
	}

	public boolean wasInterrogated() {
		return interrogated;
	}

	public void markInterrogated() {
		interrogated = true;
	}

	public void escapeFrom(LivingEntity threat) {
		hooks.onEscaped(this, threat);
		discard();
	}

	// --- Daño y rasgos ---------------------------------------------------------------

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		ServerPlayer witness = playerResponsibleFor(source);
		float modified = TraitEffects.modifyIncomingDamage(this, source, damage, trait -> {
			if (witness != null) {
				hooks.onTraitShown(this, witness, trait);
			}
		});
		// La piel gruesa se nota al primer golpe directo.
		if (witness != null && source.getEntity() == witness && traits.contains(OrcTrait.THICK_HIDE)) {
			hooks.onTraitShown(this, witness, OrcTrait.THICK_HIDE);
		}
		return super.hurtServer(level, source, modified);
	}

	/** El jugador que causa el daño o, si es fuego u otra causa sin autor, el jugador con quien pelea. */
	private @Nullable ServerPlayer playerResponsibleFor(DamageSource source) {
		if (source.getEntity() instanceof ServerPlayer player) {
			return player;
		}
		return source.getEntity() == null && getTarget() instanceof ServerPlayer target ? target : null;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof ServerPlayer player && traits.contains(OrcTrait.BRUTE)) {
			hooks.onTraitShown(this, player, OrcTrait.BRUTE);
		}
		return hit;
	}

	/** Los orcos (y sus monturas) son aliados: sus hechizos y garras no se dañan entre sí. */
	@Override
	protected boolean considersEntityAsAlly(Entity other) {
		return other instanceof OrcEntity || OrcMounts.isOrcMount(other) || super.considersEntityAsAlly(other);
	}

	public Set<OrcTrait> traits() {
		return Collections.unmodifiableSet(traits);
	}

	// --- Perfil Némesis ---------------------------------------------------------------

	public Optional<UUID> profileId() {
		return Optional.ofNullable(profileId);
	}

	public int incarnation() {
		return incarnation;
	}

	public OrcArchetype archetype() {
		return archetype;
	}

	public float sizeScale() {
		AttributeInstance scale = getAttribute(Attributes.SCALE);
		return scale == null ? 1.0f : (float) scale.getBaseValue();
	}

	/** Rango que debe recibir un perfil aún no creado (orcos generados con la aldea). */
	public Optional<OrcRank> pendingRank() {
		return Optional.ofNullable(pendingRank);
	}

	public void setPendingRank(@Nullable OrcRank rank) {
		this.pendingRank = rank;
	}

	public boolean isProfileApplied() {
		return profileApplied;
	}

	public OrcRank currentRank() {
		return pendingRank != null && profileId == null ? pendingRank : rankCache;
	}

	/**
	 * Vincula esta entidad a un perfil. {@code freshBody} indica que la entidad acaba de
	 * aparecer y debe recibir el equipo y la vida completa de su rango.
	 */
	public void bindProfile(OrcProfile profile, int incarnation, boolean freshBody) {
		this.profileId = profile.id();
		this.incarnation = incarnation;
		this.pendingRank = null;
		this.archetype = profile.archetype();
		if (freshBody) {
			OrcLoadouts.equip(this, archetype, profile.rank(), getRandom());
		}
		applyProfile(profile);
		if (freshBody) {
			setHealth(getMaxHealth());
		}
	}

	/** Aplica nombre, tamaño, rango y rasgos del perfil (por ejemplo, tras un ascenso). */
	public void applyProfile(OrcProfile profile) {
		OrcRank previousRank = rankCache;
		rankCache = profile.rank();
		traits.clear();
		traits.addAll(profile.traits());
		// La piel se guarda en el perfil para que un némesis se vea igual cada vez que vuelve.
		setSkinVariant(Math.floorMod(profile.appearance(), SKIN_VARIANTS));
		setSizeScale(profile.scale());
		applyRankModifiers(rankCache);
		TraitEffects.applyAll(this);

		if (rankCache.isNamed()) {
			setCustomName(OrcDisplayNames.fullName(profile));
			setCustomNameVisible(true);
			setPersistenceRequired();
		}
		if (rankCache.ordinal() > previousRank.ordinal() && profileApplied) {
			OrcLoadouts.equip(this, archetype, rankCache, getRandom());
		}
		profileApplied = true;
	}

	private void applyRankModifiers(OrcRank rank) {
		double bonus = rank.statMultiplier() - 1.0;
		for (var attribute : java.util.List.of(Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE)) {
			AttributeInstance instance = getAttribute(attribute);
			if (instance != null) {
				instance.addOrReplacePermanentModifier(
						new AttributeModifier(RANK_MODIFIER, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
			}
		}
	}

	/** Cambia la altura del orco; los más grandes además tienen algo más de vida. */
	public void setSizeScale(float scale) {
		AttributeInstance scaleAttribute = getAttribute(Attributes.SCALE);
		if (scaleAttribute != null) {
			scaleAttribute.setBaseValue(scale);
		}
		AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
		if (health != null) {
			health.addOrReplacePermanentModifier(
					new AttributeModifier(SIZE_MODIFIER, (scale - 1.0) * 0.8, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		}
	}

	// --- Aparición ----------------------------------------------------------------------

	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
		RandomSource random = level.getRandom();
		setSkinVariant(random.nextInt(SKIN_VARIANTS));
		if (profileId == null) {
			archetype = OrcLoadouts.randomArchetype(random);
			setSizeScale(OrcSizes.randomScale(random));
			OrcLoadouts.equip(this, archetype, currentRank(), random);
			setHealth(getMaxHealth());
		}
		combat.reassess();
		return data;
	}

	/** Configura un orco recién creado por código (emboscadas, estructuras) con un arquetipo concreto. */
	public void prepareAs(OrcArchetype archetype, RandomSource random) {
		this.archetype = archetype;
		setSizeScale(OrcSizes.randomScale(random));
		OrcLoadouts.equip(this, archetype, currentRank(), random);
		setHealth(getMaxHealth());
	}

	// --- Ataques a distancia --------------------------------------------------------------

	@Override
	public void performRangedAttack(LivingEntity target, float power) {
		if (isHolding(Items.CROSSBOW)) {
			performCrossbowAttack(this, 1.6F);
			return;
		}
		ItemStack bow = getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
		ItemStack projectile = getProjectile(bow);
		AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, power, bow);
		double dx = target.getX() - getX();
		double dy = target.getY(1.0 / 3.0) - arrow.getY();
		double dz = target.getZ() - getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		if (level() instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileUsingShoot(arrow, serverLevel, projectile, dx, dy + horizontal * 0.2F, dz, 1.6F,
					rangedAttackUncertainty(serverLevel));
		}
		playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F));
	}

	@Override
	public boolean canUseNonMeleeWeapon(ItemStack item) {
		return item.is(Items.BOW) || item.is(Items.CROSSBOW);
	}

	@Override
	public void setChargingCrossbow(boolean charging) {
		entityData.set(CHARGING_CROSSBOW, charging);
	}

	public boolean isChargingCrossbow() {
		return entityData.get(CHARGING_CROSSBOW);
	}

	@Override
	public void onCrossbowAttackPerformed() {
		noActionTime = 0;
	}

	// --- Datos sincronizados y guardado -------------------------------------------------------

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CHARGING_CROSSBOW, false);
		builder.define(SKIN_VARIANT, 0);
	}

	public int skinVariant() {
		return entityData.get(SKIN_VARIANT);
	}

	public void setSkinVariant(int variant) {
		entityData.set(SKIN_VARIANT, Mth.clamp(variant, 0, SKIN_VARIANTS - 1));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.store("Archetype", NemesisCodecs.ARCHETYPE, archetype);
		output.putInt("SkinVariant", skinVariant());
		output.storeNullable("ProfileId", UUIDUtil.CODEC, profileId);
		output.putInt("Incarnation", incarnation);
		output.storeNullable("PendingRank", NemesisCodecs.enumCodec(OrcRank.class), pendingRank);
		output.putBoolean("MountSummoned", mountSummoned);
		output.putBoolean("Interrogated", interrogated);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		archetype = input.read("Archetype", NemesisCodecs.ARCHETYPE).orElse(OrcArchetype.SWORDSMAN);
		setSkinVariant(input.getIntOr("SkinVariant", 0));
		profileId = input.read("ProfileId", UUIDUtil.CODEC).orElse(null);
		incarnation = input.getIntOr("Incarnation", 0);
		pendingRank = input.read("PendingRank", NemesisCodecs.enumCodec(OrcRank.class)).orElse(null);
		// Por defecto "ya invocada": un jinete guardado antes de la 0.4.0 no recibe una montura de golpe.
		mountSummoned = input.getBooleanOr("MountSummoned", true);
		interrogated = input.getBooleanOr("Interrogated", false);
		profileApplied = false;
		combat.reassess();
	}

	// --- Sonidos ---------------------------------------------------------------------------

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PIGLIN_BRUTE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.PIGLIN_BRUTE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PIGLIN_BRUTE_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.PIGLIN_BRUTE_STEP, 0.15F, 1.0F);
	}

	@Override
	public float getVoicePitch() {
		// Los orcos grandes tienen voz más grave.
		return super.getVoicePitch() / Math.max(0.5f, sizeScale());
	}

	/**
	 * Los capitanes y caudillos nunca desaparecen por distancia. Los soldados rasos sí, aunque
	 * tengan perfil: su memoria sigue guardada y pueden volver en una emboscada. Así un servidor
	 * con muchos jugadores no acumula orcos persistentes indefinidamente.
	 */
	@Override
	public boolean removeWhenFarAway(double distanceSquared) {
		return !currentRank().isNamed() && pendingRank == null && super.removeWhenFarAway(distanceSquared);
	}

	/**
	 * Vanilla nunca hace desaparecer a un mob que va montado. Los jinetes orcos rasos sí deben
	 * desaparecer como el resto (junto con su montura, ver {@link #remove}).
	 */
	@Override
	public boolean requiresCustomPersistence() {
		boolean ridingOwnMount = getVehicle() != null && OrcMounts.isOrcMount(getVehicle());
		return ridingOwnMount ? isLeashed() : super.requiresCustomPersistence();
	}

	/** Si el jinete desaparece (por distancia, por huir o por ser un "eco"), su montura se va con él. */
	@Override
	public void remove(RemovalReason reason) {
		Entity vehicle = getVehicle();
		if (reason == RemovalReason.DISCARDED && vehicle != null && OrcMounts.isOrcMount(vehicle)) {
			vehicle.discard();
		}
		super.remove(reason);
	}
}
