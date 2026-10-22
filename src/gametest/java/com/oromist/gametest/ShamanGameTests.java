package com.oromist.gametest;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcLoadouts;
import com.oromist.entity.OrcSquad;
import com.oromist.entity.magic.GreenFireball;
import com.oromist.entity.magic.ShamanSpells;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.registry.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Chamanes: equipo, hechizos y su protección sobre los capitanes. */
public class ShamanGameTests {
	@GameTest
	public void shamansCarryAStaffAndWearASkull(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity shaman = TestArena.orc(helper, OrcArchetype.SHAMAN, 4, 4);

		helper.assertTrue(shaman.getMainHandItem().is(OrcLoadouts.SHAMAN_STAFF), "el chamán lleva su bastón");
		helper.assertTrue(shaman.getItemBySlot(EquipmentSlot.HEAD).is(Items.SKELETON_SKULL), "el chamán lleva su calavera");
		helper.succeed();
	}

	/** El fuego verde no debe quemar casas: al chocar contra un bloque no deja fuego. */
	@GameTest(maxTicks = 60)
	public void greenFireNeverSetsBlocksOnFire(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity shaman = helper.spawnWithNoFreeWill(ModEntities.ORC, 1, 1, 1);
		GreenFireball fireball = new GreenFireball(helper.getLevel(), shaman, new Vec3(0, -1, 0));
		Vec3 above = helper.absoluteVec(new Vec3(4.5, 4, 4.5));
		fireball.setPos(above.x, above.y, above.z);
		helper.getLevel().addFreshEntity(fireball);

		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(fireball.isRemoved(), "el fuego verde debe chocar contra el suelo"))
				.thenExecuteAfter(2, () -> helper.assertBlockNotPresent(Blocks.FIRE, new BlockPos(4, 1, 4)))
				.thenSucceed();
	}

	@GameTest
	public void boneSpikesSproutTowardsTheTarget(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity shaman = helper.spawnWithNoFreeWill(ModEntities.ORC, 1, 1, 1);
		Villager target = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, 6, 1, 6);

		ShamanSpells.boneSpikes(shaman, target);

		helper.assertEntityPresent(EntityTypes.EVOKER_FANGS);
		helper.succeed();
	}

	@GameTest
	public void warBloodStrengthensAlliesThatAreFighting(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity shaman = TestArena.orc(helper, OrcArchetype.SHAMAN, 1, 1);
		OrcEntity warrior = TestArena.orc(helper, OrcArchetype.AXEMAN, 3, 3);
		Villager enemy = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, 6, 1, 6);
		warrior.setTarget(enemy);

		int blessed = ShamanSpells.warBlood(shaman);

		helper.assertTrue(blessed >= 1, "debe bendecir al guerrero que lucha");
		helper.assertTrue(warrior.hasEffect(MobEffects.STRENGTH), "el guerrero recibe fuerza");
		helper.succeed();
	}

	@GameTest
	public void aShamanNearbyProtectsTheFallen(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity captain = TestArena.orc(helper, OrcArchetype.SWORDSMAN, 2, 2);
		helper.assertFalse(OrcSquad.hasShamanNearby(captain), "todavía no hay chamán");

		TestArena.orc(helper, OrcArchetype.SHAMAN, 5, 5);

		helper.assertTrue(OrcSquad.hasShamanNearby(captain), "el chamán está cerca");
		helper.succeed();
	}
}
