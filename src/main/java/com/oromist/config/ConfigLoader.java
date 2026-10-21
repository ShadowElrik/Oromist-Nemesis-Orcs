package com.oromist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;

/**
 * Lee la configuración desde JSON y la vuelve a escribir (así el archivo siempre
 * contiene todas las opciones, incluidas las nuevas de futuras versiones).
 */
public final class ConfigLoader {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigLoader() {
	}

	public static OromistConfig load(Path file, Logger logger) {
		OromistConfig config = new OromistConfig();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				OromistConfig read = GSON.fromJson(reader, OromistConfig.class);
				if (read != null) {
					config = read;
				}
			} catch (IOException | JsonParseException e) {
				logger.warn("No se pudo leer {}, se usan valores por defecto", file, e);
			}
		}
		write(file, config, logger);
		return config;
	}

	private static void write(Path file, OromistConfig config, Logger logger) {
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			logger.warn("No se pudo escribir {}", file, e);
		}
	}
}
