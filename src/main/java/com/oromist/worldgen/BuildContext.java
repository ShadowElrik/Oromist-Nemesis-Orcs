package com.oromist.worldgen;

import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcRank;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

/**
 * Lienzo de construcción en coordenadas locales de un edificio.
 *
 * <p>Convención: {@code y = 0} es el suelo (a ras del terreno), {@code z = 0} es la
 * fachada (mira al centro de la aldea) y {@code x} va de izquierda a derecha. Para
 * orientar bloques: {@code SOUTH} apunta hacia la fachada, {@code NORTH} hacia el fondo,
 * {@code EAST} hacia +x y {@code WEST} hacia -x. La pieza se encarga de rotarlo todo.</p>
 */
public interface BuildContext {
	void set(int x, int y, int z, BlockState state);

	void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state);

	/** Vacía (aire) un volumen: elimina hierba, árboles, etc. dentro del edificio. */
	void clear(int x1, int y1, int z1, int x2, int y2, int z2);

	/** Rellena hacia abajo desde {@code y = -1} hasta tocar terreno, para que nada flote. */
	void foundation(int x, int z, BlockState state);

	void chest(int x, int y, int z, ResourceKey<LootTable> lootTable);

	/**
	 * Hace aparecer un orco de la aldea.
	 *
	 * @param leaderRank rango del perfil a crear (capitanes/caudillos); {@code null} para un soldado
	 * @param archetype  arma concreta; {@code null} para una aleatoria
	 */
	void spawnOrc(int x, int y, int z, @Nullable OrcRank leaderRank, @Nullable OrcArchetype archetype);

	RandomSource random();
}
