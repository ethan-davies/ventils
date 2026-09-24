package dev.ethann.ventils.features.dungeons;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.DungeonsCategory;
import dev.ethann.ventils.config.F7RolesCategory;
import dev.ethann.ventils.features.ChatListener;
import dev.ethann.ventils.features.HudFeature;
import dev.ethann.ventils.features.SessionListener;
import dev.ethann.ventils.features.TickListener;
import dev.ethann.ventils.features.WorldRenderListener;
import dev.ethann.ventils.features.dungeons.GoldorWaypoints.GoldorWaypoint;
import dev.ethann.ventils.features.dungeons.GoldorWaypoints.WaypointKind;
import dev.ethann.ventils.features.dungeons.GoldorWaypoints.WaypointRole;
import dev.ethann.ventils.features.dungeons.hud.TermInfoHud;
import dev.ethann.ventils.render.WorldRenderQueue;
import dev.ethann.ventils.utils.TextUtils;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import dev.ethann.ventils.utils.skyblock.dungeon.M7Phases;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InactiveWaypoints extends HudFeature implements TickListener, ChatListener, SessionListener, WorldRenderListener {
	public static final InactiveWaypoints INSTANCE = new InactiveWaypoints();

	private static final Pattern COMPLETED_REGEX = Pattern.compile(
		"^(.{1,16}) (activated|completed) a (terminal|lever|device)! \\((\\d)/(\\d)\\)$"
	);
	private static final Pattern GOLDOR_REGEX = Pattern.compile(
		"^\\[BOSS] Goldor: Who dares trespass into my domain\\?$"
	);
	private static final Pattern CORE_OPENING_REGEX = Pattern.compile("^The Core entrance is opening!$");
	private static final Pattern GATE_REGEX = Pattern.compile("^The gate has been destroyed!$");
	private static final double NAMETAG_MATCH_RANGE = 8.0;
	private static final Color OTHER_COLOR = new Color(128, 128, 128, 128);

	private final TermInfoHud termInfoHud = new TermInfoHud();
	private List<ActiveWaypoint> activeWaypoints = List.of();
	private List<ActiveWaypoint> otherWaypoints = List.of();
	private List<HudJob> hudJobs = List.of();
	private final Set<GoldorWaypoint> seenHolograms = new HashSet<>();
	private int detectedPhase;
	private boolean firstInSection;
	private boolean shouldRender;
	private boolean isComplete;
	private int lastCompleted;
	private boolean device;
	private int terminals;
	private boolean gate;
	private int section = 1;
	private int levers;
	private int tickCounter;

	private InactiveWaypoints() {
	}

	@Override
	public String name() {
		return "Inactive Waypoints";
	}

	@Override
	public boolean isEnabled() {
		return roles().inactiveWaypoints;
	}

	@Override
	public TermInfoHud hud() {
		return termInfoHud;
	}

	public boolean goldorActive() {
		return shouldRender;
	}

	public int goldorPhase() {
		return detectedPhase;
	}

	public List<HudJob> hudJobs() {
		return hudJobs;
	}

	@Override
	public void onTick(Minecraft client) {
		tickScan();
	}

	@Override
	public void onGameChat(String stripped) {
		if (!DungeonUtils.inBoss()) {
			return;
		}
		Matcher completed = COMPLETED_REGEX.matcher(stripped);
		if (completed.matches()) {
			int completedCount = parseInt(completed.group(4));
			int total = parseInt(completed.group(5));
			if (completedCount == 1) {
				firstInSection = true;
			}
			if (completedCount == total) {
				if (gate) {
					newSection();
				} else {
					isComplete = true;
				}
				return;
			}
			switch (completed.group(3)) {
				case "lever" -> levers++;
				case "terminal" -> terminals++;
				case "device" -> {
					if (!firstInSection || lastCompleted != completedCount) {
						device = true;
					}
				}
				default -> {
				}
			}
			lastCompleted = completedCount;
			return;
		}
		if (GATE_REGEX.matcher(stripped).matches()) {
			gate = true;
			if (isComplete) {
				newSection();
			}
			return;
		}
		if (GOLDOR_REGEX.matcher(stripped).matches()) {
			shouldRender = true;
			resetState();
			section = 1;
			return;
		}
		if (CORE_OPENING_REGEX.matcher(stripped).matches()) {
			shouldRender = false;
			resetState();
		}
	}

	@Override
	public void onJoin() {
		resetAll();
	}

	@Override
	public void onDisconnect() {
		resetAll();
	}

	@Override
	public void onWorldRender() {
		F7RolesCategory cfg = roles();
		if (!cfg.inactiveWaypoints || DungeonUtils.getF7Phase() != M7Phases.P3) {
			return;
		}
		if (activeWaypoints.isEmpty() && otherWaypoints.isEmpty()) {
			return;
		}
		ActiveWaypoint tracerTarget = cfg.render.tracer ? closestJob(activeWaypoints) : null;
		for (ActiveWaypoint active : activeWaypoints) {
			GoldorWaypoint waypoint = active.waypoint();
			Color color = colorFor(cfg, active.role()).getEffectiveColour();
			BlockPos pos = active.renderPos();
			if (cfg.render.box) {
				WorldRenderQueue.box(new AABB(pos), color, cfg.render.depthCheck, cfg.render.fill);
			}
			if (cfg.render.text) {
				String label = waypoint.name();
				if (active.role() == WaypointRole.STACKED) {
					label += " (stacked)";
				}
				WorldRenderQueue.text(label, Vec3.atCenterOf(pos).add(0, 1.75, 0), 2.25f, false);
			}
			if (active == tracerTarget) {
				WorldRenderQueue.tracer(Vec3.atCenterOf(pos), color, cfg.render.depthCheck);
			}
		}
		if (cfg.render.otherInactive) {
			for (ActiveWaypoint other : otherWaypoints) {
				WorldRenderQueue.box(new AABB(other.renderPos()), OTHER_COLOR, cfg.render.depthCheck, cfg.render.fill);
			}
		}
	}

	private void tickScan() {
		F7RolesCategory cfg = roles();
		if (!cfg.inactiveWaypoints || !shouldRender || DungeonUtils.getF7Phase() != M7Phases.P3) {
			clearWaypoints();
			return;
		}
		if (++tickCounter < 10) {
			return;
		}
		tickCounter = 0;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || mc.player == null) {
			clearWaypoints();
			return;
		}
		Map<GoldorWaypoint, ArmorStand> holograms = matchNametags(level);
		int phaseFromTags = phaseFromMatches(holograms);
		if (phaseFromTags >= 0 && phaseFromTags != detectedPhase) {
			detectedPhase = phaseFromTags;
			section = detectedPhase + 1;
			seenHolograms.clear();
			resetHudCounts();
		}
		List<GoldorWaypoint> phaseWaypoints = GoldorWaypoints.forPhase(detectedPhase);
		if (phaseWaypoints.isEmpty()) {
			clearWaypoints();
			return;
		}
		Set<GoldorWaypoint> done = new HashSet<>();
		for (GoldorWaypoint waypoint : phaseWaypoints) {
			if (isDone(level, waypoint, holograms.get(waypoint))) {
				done.add(waypoint);
			}
		}
		Predicate<GoldorWaypoint> doneCheck = done::contains;
		List<ActiveWaypoint> next = new ArrayList<>();
		List<ActiveWaypoint> nextOther = new ArrayList<>();
		List<HudJob> nextHud = new ArrayList<>();
		for (GoldorWaypoint waypoint : phaseWaypoints) {
			if (!kindEnabled(cfg, waypoint.kind())) {
				continue;
			}
			WaypointRole role = GoldorWaypoints.resolve(
				waypoint,
				dungeons().dungeonClass,
				phaseWaypoints,
				doneCheck
			);
			boolean finished = done.contains(waypoint);
			if (role == null) {
				if (cfg.render.otherInactive && !finished) {
					nextOther.add(new ActiveWaypoint(waypoint, null, renderPos(level, waypoint, holograms.get(waypoint))));
				}
				continue;
			}
			nextHud.add(new HudJob(waypoint.name(), role, finished));
			if (finished) {
				continue;
			}
			next.add(new ActiveWaypoint(waypoint, role, renderPos(level, waypoint, holograms.get(waypoint))));
		}
		activeWaypoints = List.copyOf(next);
		otherWaypoints = List.copyOf(nextOther);
		hudJobs = List.copyOf(nextHud);
		applyHideDefault(level, phaseWaypoints, cfg.render.hideDefault);
	}

	private static boolean kindEnabled(F7RolesCategory cfg, WaypointKind kind) {
		return switch (kind) {
			case TERMINAL -> cfg.kinds.terminals;
			case DEVICE -> cfg.kinds.devices;
			case LEVER -> cfg.kinds.levers;
		};
	}

	private boolean isDone(ClientLevel level, GoldorWaypoint waypoint, ArmorStand matchedHologram) {
		BlockPos pos = waypoint.pos();
		if (!level.isLoaded(pos)) {
			return false;
		}
		if (waypoint.kind() == WaypointKind.LEVER && isLeverPoweredNear(level, pos)) {
			return true;
		}
		ArmorStand hologram = matchedHologram != null ? matchedHologram : findInactiveHologram(level, waypoint);
		if (hologram != null) {
			seenHolograms.add(waypoint);
			return false;
		}
		return seenHolograms.contains(waypoint);
	}

	private static Map<GoldorWaypoint, ArmorStand> matchNametags(ClientLevel level) {
		Map<GoldorWaypoint, ArmorStand> matches = new HashMap<>();
		Map<GoldorWaypoint, Double> distances = new HashMap<>();
		for (Entity entity : level.entitiesForRendering()) {
			if (!(entity instanceof ArmorStand stand)) {
				continue;
			}
			WaypointKind kind = kindFromName(TextUtils.strip(stand.getName().getString()));
			if (kind == null) {
				continue;
			}
			GoldorWaypoint nearest = GoldorWaypoints.nearest(kind, stand.position(), NAMETAG_MATCH_RANGE);
			if (nearest == null) {
				continue;
			}
			double dist = Vec3.atCenterOf(nearest.pos()).distanceToSqr(stand.position());
			Double existing = distances.get(nearest);
			if (existing == null || dist < existing) {
				matches.put(nearest, stand);
				distances.put(nearest, dist);
			}
		}
		return matches;
	}

	private static int phaseFromMatches(Map<GoldorWaypoint, ArmorStand> holograms) {
		int phase = -1;
		for (GoldorWaypoint waypoint : holograms.keySet()) {
			if (phase < 0 || waypoint.phase() < phase) {
				phase = waypoint.phase();
			}
		}
		return phase;
	}

	private static boolean isLeverPoweredNear(ClientLevel level, BlockPos center) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					BlockState state = level.getBlockState(center.offset(dx, dy, dz));
					if (state.is(Blocks.LEVER) && state.getValue(LeverBlock.POWERED)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static ArmorStand findInactiveHologram(ClientLevel level, GoldorWaypoint waypoint) {
		AABB area = hologramArea(waypoint.pos());
		for (ArmorStand stand : level.getEntitiesOfClass(ArmorStand.class, area, candidate -> true)) {
			if (matchesKind(TextUtils.strip(stand.getName().getString()), waypoint.kind())) {
				return stand;
			}
		}
		return null;
	}

	private static void applyHideDefault(ClientLevel level, List<GoldorWaypoint> phaseWaypoints, boolean hideDefault) {
		for (GoldorWaypoint waypoint : phaseWaypoints) {
			if (!level.isLoaded(waypoint.pos())) {
				continue;
			}
			AABB area = hologramArea(waypoint.pos());
			for (ArmorStand stand : level.getEntitiesOfClass(ArmorStand.class, area, candidate -> true)) {
				if (matchesKind(TextUtils.strip(stand.getName().getString()), waypoint.kind())) {
					stand.setCustomNameVisible(!hideDefault);
				}
			}
		}
	}

	private static AABB hologramArea(BlockPos pos) {
		return new AABB(pos).inflate(1.0, 2.0, 1.0);
	}

	private static boolean matchesKind(String name, WaypointKind kind) {
		return kindFromName(name) == kind;
	}

	private static WaypointKind kindFromName(String name) {
		if ("Inactive Terminal".equals(name)) {
			return WaypointKind.TERMINAL;
		}
		if ("Inactive".equals(name) || "CLICK HERE".equalsIgnoreCase(name)) {
			return WaypointKind.DEVICE;
		}
		if ("Not Activated".equals(name)) {
			return WaypointKind.LEVER;
		}
		return null;
	}

	private static BlockPos renderPos(ClientLevel level, GoldorWaypoint waypoint, ArmorStand matchedHologram) {
		ArmorStand hologram = matchedHologram != null ? matchedHologram : findInactiveHologram(level, waypoint);
		return hologram != null ? hologram.blockPosition() : waypoint.pos();
	}

	private static ActiveWaypoint closestJob(List<ActiveWaypoint> waypoints) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return null;
		}
		Vec3 player = mc.player.position();
		ActiveWaypoint closestPriority = null;
		double closestPriorityDist = Double.MAX_VALUE;
		ActiveWaypoint closestOptional = null;
		double closestOptionalDist = Double.MAX_VALUE;
		for (ActiveWaypoint active : waypoints) {
			double dist = Vec3.atCenterOf(active.renderPos()).distanceToSqr(player);
			if (active.role() == WaypointRole.REQUIRED || active.role() == WaypointRole.STACKED) {
				if (dist < closestPriorityDist) {
					closestPriorityDist = dist;
					closestPriority = active;
				}
			} else if (dist < closestOptionalDist) {
				closestOptionalDist = dist;
				closestOptional = active;
			}
		}
		return closestPriority != null ? closestPriority : closestOptional;
	}

	private static ChromaColour colorFor(F7RolesCategory cfg, WaypointRole role) {
		return switch (role) {
			case REQUIRED -> cfg.colors.required;
			case STACKED -> cfg.colors.stacked;
			case OPTIONAL -> cfg.colors.optional;
		};
	}

	private void resetAll() {
		shouldRender = false;
		resetState();
	}

	private void resetState() {
		clearWaypoints();
		seenHolograms.clear();
		firstInSection = false;
		lastCompleted = 0;
		isComplete = false;
		device = false;
		terminals = 0;
		gate = false;
		section = 1;
		detectedPhase = 0;
		levers = 0;
		tickCounter = 0;
	}

	private void newSection() {
		resetHudCounts();
		seenHolograms.clear();
		clearWaypoints();
	}

	private void clearWaypoints() {
		activeWaypoints = List.of();
		otherWaypoints = List.of();
		hudJobs = List.of();
	}

	private void resetHudCounts() {
		firstInSection = false;
		isComplete = false;
		device = false;
		terminals = 0;
		gate = false;
		levers = 0;
	}

	private static int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	private static F7RolesCategory roles() {
		return Ventils.CONFIG.getInstance().dungeons.f7Roles;
	}

	private static DungeonsCategory dungeons() {
		return Ventils.CONFIG.getInstance().dungeons;
	}

	public record HudJob(String name, WaypointRole role, boolean done) {
	}

	private record ActiveWaypoint(GoldorWaypoint waypoint, WaypointRole role, BlockPos renderPos) {
	}
}
