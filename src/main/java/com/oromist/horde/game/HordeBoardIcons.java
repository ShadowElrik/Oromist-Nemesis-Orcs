package com.oromist.horde.game;

import com.oromist.dialogue.MemoryDialogueSelector;
import com.oromist.entity.OrcDisplayNames;
import com.oromist.horde.HordeBoard;
import com.oromist.horde.HordeBoardLayout;
import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Convierte cada casilla del tablero en el objeto que se ve en el menú: el arma del orco con
 * su ficha como descripción, etiquetas de grupo, la ayuda, etc. Solo presentación.
 */
public final class HordeBoardIcons {
	private HordeBoardIcons() {
	}

	/** @param now tick actual del mundo, para escribir "hace N días" */
	public static ItemStack icon(HordeBoardLayout.Cell cell, long now) {
		return switch (cell) {
			case HordeBoardLayout.Empty empty -> ItemStack.EMPTY;
			case HordeBoardLayout.Filler filler -> named(Items.STAINED_GLASS_PANE.black(), Component.literal(" "), List.of());
			case HordeBoardLayout.Info info -> infoIcon(info);
			case HordeBoardLayout.SectionLabel label -> sectionIcon(label);
			case HordeBoardLayout.Overflow overflow -> named(Items.PAPER,
					text(Component.translatable("horde.oromist-npc.more", overflow.hidden()), ChatFormatting.GRAY), List.of());
			case HordeBoardLayout.OrcCell orc -> orcIcon(orc.entry(), now);
		};
	}

	// --- Orcos -----------------------------------------------------------------------------

	private static ItemStack orcIcon(HordeBoard.Entry entry, long now) {
		OrcProfile profile = entry.profile();
		MutableComponent title = OrcDisplayNames.fullName(profile).copy();
		if (entry.nemesis()) {
			title = Component.literal("☠ ").withStyle(ChatFormatting.DARK_RED).append(title);
		}
		ItemStack stack = named(entry.nemesis() ? Items.WITHER_SKELETON_SKULL : weaponOf(profile.archetype()), title,
				describe(entry, now));
		// Los capitanes y caudillos brillan, como objetos encantados.
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, profile.rank().isNamed());
		return stack;
	}

	/** La ficha del orco: su resumen y sus recuerdos más importantes. */
	public static List<Component> describe(HordeBoard.Entry entry, long now) {
		List<Component> lines = new ArrayList<>(summary(entry));
		if (!entry.memories().isEmpty()) {
			lines.add(Component.empty());
			lines.add(text(Component.translatable("horde.oromist-npc.memories"), ChatFormatting.GOLD));
			for (Memory memory : entry.memories()) {
				lines.add(text(Component.literal("- ").append(memoryText(memory, now)), ChatFormatting.WHITE));
			}
		}
		return lines;
	}

	/** Rango y arquetipo, rasgos, rencor y cuántas veces volvió de la muerte. */
	public static List<Component> summary(HordeBoard.Entry entry) {
		OrcProfile profile = entry.profile();
		List<Component> lines = new ArrayList<>();
		lines.add(text(Component.empty().append(OrcDisplayNames.rankName(profile.rank()))
				.append(" · ").append(Component.translatable(profile.archetype().translationKey())), ChatFormatting.GRAY));
		lines.add(text(Component.translatable("horde.oromist-npc.traits", traitList(entry)), ChatFormatting.GRAY));
		lines.add(text(Component.translatable("horde.oromist-npc.grudge", entry.grudge()), ChatFormatting.RED));
		if (profile.timesCheatedDeath() > 0) {
			lines.add(text(Component.translatable("horde.oromist-npc.cheated_death", profile.timesCheatedDeath()),
					ChatFormatting.DARK_GREEN));
		}
		return lines;
	}

	/** "Te mató en Taiga (hace 3 días)". */
	public static Component memoryText(Memory memory, long now) {
		return Component.translatable("memory.oromist-npc." + memory.type().name().toLowerCase(Locale.ROOT),
				Component.translatable(MemoryDialogueSelector.placeTranslationKey(memory.place())),
				memory.daysAgo(now));
	}

	/** Rasgos conocidos por su nombre y los demás como "???". */
	private static Component traitList(HordeBoard.Entry entry) {
		if (entry.knownTraits().isEmpty() && entry.hiddenTraitCount() == 0) {
			return Component.translatable("horde.oromist-npc.no_traits");
		}
		MutableComponent list = Component.empty();
		boolean first = true;
		for (OrcTrait trait : entry.knownTraits()) {
			list.append(first ? Component.empty() : Component.literal(", ")).append(OrcDisplayNames.traitName(trait));
			first = false;
		}
		for (int i = 0; i < entry.hiddenTraitCount(); i++) {
			list.append(first ? Component.empty() : Component.literal(", "))
					.append(Component.translatable("command.oromist-npc.unknown_trait").withStyle(ChatFormatting.DARK_GRAY));
			first = false;
		}
		return list;
	}

	private static Item weaponOf(OrcArchetype archetype) {
		return switch (archetype) {
			case SWORDSMAN -> Items.IRON_SWORD;
			case AXEMAN -> Items.IRON_AXE;
			case ARCHER -> Items.BOW;
			case CROSSBOWMAN -> Items.CROSSBOW;
			case SHAMAN -> Items.BLAZE_ROD;
			case LANCER -> Items.IRON_SPEAR;
		};
	}

	// --- Etiquetas y ayuda ----------------------------------------------------------------

	private static ItemStack sectionIcon(HordeBoardLayout.SectionLabel label) {
		Item item = switch (label.section()) {
			case WARCHIEFS -> Items.GOLDEN_HELMET;
			case CAPTAINS -> Items.IRON_HELMET;
			case GRUNTS -> Items.LEATHER_HELMET;
		};
		String key = "horde.oromist-npc.section." + label.section().name().toLowerCase(Locale.ROOT);
		return named(item, text(Component.translatable(key), ChatFormatting.GOLD),
				List.of(text(Component.translatable("horde.oromist-npc.count", label.count()), ChatFormatting.GRAY)));
	}

	private static ItemStack infoIcon(HordeBoardLayout.Info info) {
		List<Component> lines = new ArrayList<>();
		if (info.boardIsEmpty()) {
			lines.add(text(Component.translatable("horde.oromist-npc.empty"), ChatFormatting.RED));
			lines.add(Component.empty());
		}
		for (int i = 0; i < 4; i++) {
			lines.add(text(Component.translatable("horde.oromist-npc.help." + i), ChatFormatting.GRAY));
		}
		return named(Items.BOOK, text(Component.translatable("horde.oromist-npc.help_title"), ChatFormatting.AQUA), lines);
	}

	// --- Utilidades ------------------------------------------------------------------------

	/** Un objeto de exhibición: nombre, descripción y sin los datos técnicos (daño, durabilidad...). */
	private static ItemStack named(Item item, Component name, List<Component> lore) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(style -> style.withItalic(false)));
		stack.set(DataComponents.LORE, new ItemLore(lore));
		stack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT
				.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true)
				.withHidden(DataComponents.ENCHANTMENTS, true));
		return stack;
	}

	/** Texto de descripción sin la cursiva morada por defecto de Minecraft. */
	private static Component text(Component content, ChatFormatting color) {
		return content.copy().withStyle(color).withStyle(style -> style.withItalic(false));
	}
}
