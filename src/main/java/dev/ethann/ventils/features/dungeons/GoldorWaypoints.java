package dev.ethann.ventils.features.dungeons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

public final class GoldorWaypoints {
	private static final String RESOURCE = "/dungeons/goldorwaypoints.json";
	private static final List<GoldorWaypoint> ALL = load();

	private GoldorWaypoints() {
	}

	public static List<GoldorWaypoint> all() {
		return ALL;
	}

	public static List<GoldorWaypoint> forPhase(int phase) {
		List<GoldorWaypoint> matches = new ArrayList<>();
		for (GoldorWaypoint waypoint : ALL) {
			if (waypoint.phase() == phase) {
				matches.add(waypoint);
			}
		}
		return matches;
	}

	public static GoldorWaypoint nearest(WaypointKind kind, Vec3 pos, double maxDistance) {
		GoldorWaypoint best = null;
		double bestDist = maxDistance * maxDistance;
		for (GoldorWaypoint waypoint : ALL) {
			if (waypoint.kind() != kind) {
				continue;
			}
			double dist = Vec3.atCenterOf(waypoint.pos()).distanceToSqr(pos);
			if (dist < bestDist) {
				bestDist = dist;
				best = waypoint;
			}
		}
		return best;
	}

	public static WaypointRole resolve(
		GoldorWaypoint waypoint,
		DungeonClass playerClass,
		List<GoldorWaypoint> phaseWaypoints,
		Predicate<GoldorWaypoint> isDone
	) {
		List<GoldorAssignment> mine = assignmentsFor(waypoint, playerClass);
		if (mine.isEmpty()) {
			return null;
		}
		for (GoldorAssignment assignment : mine) {
			if (assignment.stack() && conditionPasses(assignment, phaseWaypoints, isDone, new HashSet<>())) {
				return WaypointRole.STACKED;
			}
		}
		for (GoldorAssignment assignment : mine) {
			if (assignment.required() && !assignment.stack()) {
				return WaypointRole.REQUIRED;
			}
		}
		return WaypointRole.OPTIONAL;
	}

	private static List<GoldorAssignment> assignmentsFor(GoldorWaypoint waypoint, DungeonClass playerClass) {
		List<GoldorAssignment> mine = new ArrayList<>();
		for (GoldorAssignment assignment : waypoint.assignments()) {
			if (assignment.dungeonClass() == playerClass) {
				mine.add(assignment);
			}
		}
		return mine;
	}

	private static boolean conditionPasses(
		GoldorAssignment assignment,
		List<GoldorWaypoint> phaseWaypoints,
		Predicate<GoldorWaypoint> isDone,
		Set<GoldorAssignment> visiting
	) {
		GoldorCondition condition = assignment.condition();
		if (condition == null) {
			return true;
		}
		if (!visiting.add(assignment)) {
			return false;
		}
		return switch (condition.type()) {
			case "objective_incomplete" -> {
				GoldorWaypoint target = findByName(phaseWaypoints, condition.objective());
				yield target == null || !isDone.test(target);
			}
			case "stack" -> {
				GoldorWaypoint target = findByName(phaseWaypoints, condition.target());
				if (target == null) {
					yield false;
				}
				for (GoldorAssignment other : target.assignments()) {
					if (!other.stack() || other == assignment) {
						continue;
					}
					if (conditionPasses(other, phaseWaypoints, isDone, visiting)) {
						yield true;
					}
				}
				yield false;
			}
			default -> false;
		};
	}

	private static GoldorWaypoint findByName(List<GoldorWaypoint> phaseWaypoints, String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		for (GoldorWaypoint waypoint : phaseWaypoints) {
			if (waypoint.name().equalsIgnoreCase(name)) {
				return waypoint;
			}
		}
		return null;
	}

	private static List<GoldorWaypoint> load() {
		try (InputStream stream = GoldorWaypoints.class.getResourceAsStream(RESOURCE)) {
			if (stream == null) {
				Ventils.LOGGER.error("Missing Goldor waypoint resource {}", RESOURCE);
				return List.of();
			}
			JsonArray array = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
			List<GoldorWaypoint> waypoints = new ArrayList<>();
			for (JsonElement element : array) {
				GoldorWaypoint waypoint = parseWaypoint(element.getAsJsonObject());
				if (waypoint != null) {
					waypoints.add(waypoint);
				}
			}
			return List.copyOf(waypoints);
		} catch (Exception exception) {
			Ventils.LOGGER.error("Failed to load Goldor waypoints", exception);
			return List.of();
		}
	}

	private static GoldorWaypoint parseWaypoint(JsonObject object) {
		WaypointKind kind = WaypointKind.fromJson(string(object, "kind"));
		if (kind == null) {
			return null;
		}
		JsonArray posArray = object.getAsJsonArray("pos");
		if (posArray == null || posArray.size() < 3) {
			return null;
		}
		BlockPos pos = new BlockPos(posArray.get(0).getAsInt(), posArray.get(1).getAsInt(), posArray.get(2).getAsInt());
		List<GoldorAssignment> assignments = new ArrayList<>();
		JsonArray assignmentArray = object.getAsJsonArray("assignments");
		if (assignmentArray != null) {
			for (JsonElement element : assignmentArray) {
				GoldorAssignment assignment = parseAssignment(element.getAsJsonObject());
				if (assignment != null) {
					assignments.add(assignment);
				}
			}
		}
		return new GoldorWaypoint(
			kind,
			object.has("phase") ? object.get("phase").getAsInt() : 0,
			string(object, "name"),
			pos,
			List.copyOf(assignments)
		);
	}

	private static GoldorAssignment parseAssignment(JsonObject object) {
		DungeonClass dungeonClass = DungeonClass.fromLabel(string(object, "class"));
		if (dungeonClass == null) {
			return null;
		}
		return new GoldorAssignment(
			dungeonClass,
			bool(object, "required", false),
			bool(object, "stack", false),
			parseCondition(object.get("condition"))
		);
	}

	private static GoldorCondition parseCondition(JsonElement element) {
		if (element == null || !element.isJsonObject()) {
			return null;
		}
		JsonObject object = element.getAsJsonObject();
		return new GoldorCondition(
			string(object, "type"),
			string(object, "target"),
			string(object, "objective")
		);
	}

	private static String string(JsonObject object, String key) {
		return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
	}

	private static boolean bool(JsonObject object, String key, boolean fallback) {
		return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsBoolean() : fallback;
	}

	public enum WaypointKind {
		TERMINAL,
		DEVICE,
		LEVER;

		static WaypointKind fromJson(String kind) {
			if (kind == null || kind.isBlank()) {
				return null;
			}
			return switch (kind.toLowerCase(Locale.ROOT)) {
				case "terminal" -> TERMINAL;
				case "device" -> DEVICE;
				case "lever" -> LEVER;
				default -> null;
			};
		}
	}

	public enum WaypointRole {
		REQUIRED,
		STACKED,
		OPTIONAL
	}

	public record GoldorWaypoint(
		WaypointKind kind,
		int phase,
		String name,
		BlockPos pos,
		List<GoldorAssignment> assignments
	) {
	}

	public record GoldorAssignment(
		DungeonClass dungeonClass,
		boolean required,
		boolean stack,
		GoldorCondition condition
	) {
	}

	public record GoldorCondition(
		String type,
		String target,
		String objective
	) {
	}
}
