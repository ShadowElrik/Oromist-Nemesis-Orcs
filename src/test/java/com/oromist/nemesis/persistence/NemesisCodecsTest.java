package com.oromist.nemesis.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.oromist.nemesis.domain.Memory;
import com.oromist.nemesis.domain.MemoryType;
import com.oromist.nemesis.domain.OrcArchetype;
import com.oromist.nemesis.domain.OrcName;
import com.oromist.nemesis.domain.OrcProfile;
import com.oromist.nemesis.domain.OrcRank;
import com.oromist.nemesis.domain.OrcTrait;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NemesisCodecsTest {
	private static final UUID PLAYER = UUID.randomUUID();

	private static OrcProfile sampleProfile() {
		return new OrcProfile(UUID.randomUUID(), new OrcName("Gorbag", "epithet.oromist-npc.the_cruel"), OrcArchetype.CROSSBOWMAN,
				1.27f, 2, EnumSet.of(OrcTrait.BRUTE, OrcTrait.COWARD),
				List.of(new Memory(MemoryType.KILLED_PLAYER, PLAYER, "Steve", 123456L, "minecraft:plains")),
				7, 2, 4, true);
	}

	private static JsonObject encode(OrcProfile profile) {
		return NemesisCodecs.PROFILE.encodeStart(JsonOps.INSTANCE, profile).getOrThrow().getAsJsonObject();
	}

	private static OrcProfile decode(JsonElement json) {
		return NemesisCodecs.PROFILE.parse(JsonOps.INSTANCE, json).getOrThrow();
	}

	@Test
	void profileSurvivesARoundTrip() {
		OrcProfile original = sampleProfile();

		OrcProfile copy = decode(encode(original));

		assertEquals(original.id(), copy.id());
		assertEquals(original.name(), copy.name());
		assertEquals(original.archetype(), copy.archetype());
		assertEquals(original.scale(), copy.scale());
		assertEquals(original.appearance(), copy.appearance());
		assertEquals(original.traits(), copy.traits());
		assertEquals(original.memories(), copy.memories());
		assertEquals(OrcRank.CAPTAIN, copy.rank());
		assertEquals(2, copy.timesCheatedDeath());
		assertEquals(4, copy.incarnation());
		assertTrue(copy.isAlive());
	}

	/** Datos de una versión futura (rasgo o recuerdo desconocido) no deben romper la carga. */
	@Test
	void unknownTraitsAndMemoriesAreSkippedInsteadOfFailing() {
		JsonObject json = encode(sampleProfile());
		json.getAsJsonArray("traits").add("trait_from_the_future");
		JsonObject strangeMemory = json.getAsJsonArray("memories").get(0).getAsJsonObject().deepCopy();
		strangeMemory.addProperty("type", "memory_from_the_future");
		json.getAsJsonArray("memories").add(strangeMemory);

		OrcProfile copy = decode(json);

		assertEquals(EnumSet.of(OrcTrait.BRUTE, OrcTrait.COWARD), copy.traits());
		assertEquals(1, copy.memories().size());
	}

	/** Los perfiles de la 0.1.0 no guardaban la piel: debe salir la misma que entonces (hash del id). */
	@Test
	void profilesFromVersion010KeepTheirOldSkin() {
		OrcProfile original = sampleProfile();
		JsonObject json = encode(original);
		json.remove("appearance");

		OrcProfile loaded = decode(json);

		assertEquals(Math.floorMod(original.id().hashCode(), 3), Math.floorMod(loaded.appearance(), 3));
	}

	/** Un perfil corrupto en la lista no debe borrar al resto de orcos. */
	@Test
	void corruptProfilesAreSkippedAndTheRestAreKept() {
		JsonArray list = new JsonArray();
		list.add(encode(sampleProfile()));
		JsonObject broken = new JsonObject();
		broken.addProperty("archetype", "dragon");
		list.add(broken);
		list.add(encode(sampleProfile()));

		List<OrcProfile> loaded = NemesisCodecs.lenientList(NemesisCodecs.PROFILE).parse(JsonOps.INSTANCE, list).getOrThrow();

		assertEquals(2, loaded.size());
	}
}
