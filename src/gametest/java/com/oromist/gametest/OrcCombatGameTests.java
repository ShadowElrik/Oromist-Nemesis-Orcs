package com.oromist.gametest;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcSquad;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcRank;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;

/** Combate en grupo e interrogatorios. */
public class OrcCombatGameTests {
	@GameTest
	public void aWarCryRalliesIdleOrcs(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity caller = TestArena.orc(helper, OrcArchetype.SWORDSMAN, 1, 1);
		OrcEntity listener = TestArena.orc(helper, OrcArchetype.ARCHER, 6, 6);
		Villager prey = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, 1, 1, 6);

		int answered = OrcSquad.warCry(caller, prey);

		helper.assertTrue(answered >= 1, "al menos un orco responde");
		helper.assertTrue(listener.getTarget() == prey, "el orco que estaba ocioso se une a la caza");
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void aCaptainInspiresNearbyGrunts(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity captain = TestArena.orc(helper, OrcArchetype.AXEMAN, 2, 2);
		captain.setPendingRank(OrcRank.CAPTAIN);
		OrcEntity grunt = TestArena.orc(helper, OrcArchetype.SWORDSMAN, 5, 5);

		helper.assertTrue(OrcSquad.hasLeaderNearby(grunt), "el capitán está cerca del soldado");
		helper.succeedWhen(() -> {
			helper.assertTrue(grunt.isInspired(), "el soldado se siente inspirado");
			helper.assertFalse(captain.isInspired(), "los líderes no se inspiran a sí mismos");
		});
	}

	@GameTest
	public void onlyBadlyWoundedOrcsCanBeInterrogated(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity orc = TestArena.orc(helper, OrcArchetype.SWORDSMAN, 4, 4);

		helper.assertFalse(orc.isBroken(), "un orco sano no habla");
		orc.setHealth(orc.getMaxHealth() * 0.2f);
		helper.assertTrue(orc.isBroken(), "un orco malherido sí");
		helper.succeed();
	}
}
