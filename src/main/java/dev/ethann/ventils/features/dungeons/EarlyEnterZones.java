package dev.ethann.ventils.features.dungeons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ethann.ventils.Ventils;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EarlyEnterZones {
	private static final String RESOURCE = "/dungeons/earlyenter.json";
	private static final List<EarlyEnterZone> ALL = load();

	private EarlyEnterZones() {
	}

	public static List<EarlyEnterZone> all() {
		return ALL;
	}

	private static List<EarlyEnterZone> load() {
		try (InputStream stream = EarlyEnterZones.class.getResourceAsStream(RESOURCE)) {
			if (stream == null) {
				Ventils.LOGGER.error("Missing early enter resource {}", RESOURCE);
				return List.of();
			}
			JsonArray array = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
			List<EarlyEnterZone> zones = new ArrayList<>();
			for (JsonElement element : array) {
				EarlyEnterZone zone = parse(element.getAsJsonObject());
				if (zone != null) {
					zones.add(zone);
				}
			}
			return List.copyOf(zones);
		} catch (Exception exception) {
			Ventils.LOGGER.error("Failed to load early enter zones", exception);
			return List.of();
		}
	}

	private static EarlyEnterZone parse(JsonObject object) {
		String id = string(object, "id");
		String label = string(object, "label");
		if (id.isBlank() || label.isBlank()) {
			return null;
		}
		int phase = object.has("phase") && !object.get("phase").isJsonNull() ? object.get("phase").getAsInt() : 0;
		String shape = string(object, "shape").toLowerCase(Locale.ROOT);
		if ("sphere".equals(shape)) {
			double x = object.get("x").getAsDouble();
			double y = object.get("y").getAsDouble();
			double z = object.get("z").getAsDouble();
			double radius = object.get("radius").getAsDouble();
			return EarlyEnterZone.sphere(id, label, phase, new Vec3(x, y, z), radius);
		}
		if ("box".equals(shape)) {
			JsonArray min = object.getAsJsonArray("min");
			JsonArray max = object.getAsJsonArray("max");
			if (min == null || max == null || min.size() < 3 || max.size() < 3) {
				return null;
			}
			AABB box = new AABB(
				min.get(0).getAsDouble(),
				min.get(1).getAsDouble(),
				min.get(2).getAsDouble(),
				max.get(0).getAsDouble(),
				max.get(1).getAsDouble(),
				max.get(2).getAsDouble()
			);
			return EarlyEnterZone.box(id, label, phase, box);
		}
		return null;
	}

	private static String string(JsonObject object, String key) {
		return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
	}

	public record EarlyEnterZone(String id, String label, int phase, Vec3 center, Double radius, AABB box) {
		static EarlyEnterZone sphere(String id, String label, int phase, Vec3 center, double radius) {
			return new EarlyEnterZone(id, label, phase, center, radius, null);
		}

		static EarlyEnterZone box(String id, String label, int phase, AABB box) {
			return new EarlyEnterZone(id, label, phase, box.getCenter(), null, box);
		}

		boolean contains(Vec3 pos) {
			if (radius != null) {
				return pos.distanceToSqr(center) <= radius * radius;
			}
			return box != null && box.contains(pos);
		}

		boolean inCapturePhase(int goldorPhase) {
			return goldorPhase == phase;
		}

		boolean inDisplayWindow(int goldorPhase) {
			return goldorPhase >= phase && goldorPhase <= phase + 1;
		}
	}
}
