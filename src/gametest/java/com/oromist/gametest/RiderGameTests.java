package com.oromist.gametest;

import com.oromist.entity.OrcEntity;
import com.oromist.entity.OrcMounts;
import com.oromist.nemesis.domain.OrcArchetype;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;

/** Jinetes orcos y sus caballos zombi. */
public class RiderGameTests {
	@GameTest(maxTicks = 60)
	public void lancerRidesAZombieHorseWithASpear(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity lancer = TestArena.orc(helper, OrcArchetype.LANCER, 4, 4);

		helper.assertTrue(lancer.getMainHandItem().has(DataComponents.KINETIC_WEAPON), "el jinete debe llevar una lanza");
		helper.succeedWhen(() -> helper.assertTrue(isMounted(lancer), "el jinete debe ir montado en su caballo zombi"));
	}

	@GameTest(maxTicks = 60)
	public void aRiderThatDisappearsTakesItsMountWithIt(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity lancer = TestArena.orc(helper, OrcArchetype.LANCER, 4, 4);
		AtomicReference<Entity> mount = new AtomicReference<>();

		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(isMounted(lancer), "el jinete debe ir montado"))
				.thenExecute(() -> {
					mount.set(lancer.getVehicle());
					lancer.discard(); // Lo mismo que ocurre cuando desaparece por distancia o huye.
				})
				.thenExecuteAfter(1, () -> helper.assertTrue(mount.get().isRemoved(), "la montura debe irse con su jinete"))
				.thenSucceed();
	}

	@GameTest(maxTicks = 40)
	public void footSoldiersDoNotGetAMount(GameTestHelper helper) {
		TestArena.prepare(helper);
		OrcEntity swordsman = TestArena.orc(helper, OrcArchetype.SWORDSMAN, 4, 4);

		helper.runAfterDelay(30, () -> {
			helper.assertFalse(swordsman.isPassenger(), "un espadachín no monta");
			helper.succeed();
		});
	}

	private static boolean isMounted(OrcEntity orc) {
		return orc.getVehicle() != null && OrcMounts.isOrcMount(orc.getVehicle());
	}
}
