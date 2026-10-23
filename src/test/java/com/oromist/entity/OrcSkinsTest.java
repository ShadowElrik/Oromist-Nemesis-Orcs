package com.oromist.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class OrcSkinsTest {
	/** Si se sube SKIN_VARIANTS sin generar las texturas, el juego mostraría orcos sin textura. */
	@Test
	void everySkinVariantHasItsTexture() throws IOException {
		for (int i = 0; i < OrcEntity.SKIN_VARIANTS; i++) {
			String path = "/assets/oromist-npc/textures/entity/orc/orc_" + i + ".png";
			try (InputStream texture = OrcSkinsTest.class.getResourceAsStream(path)) {
				assertNotNull(texture, "falta la textura " + path);
			}
		}
	}
}
