package dev.ethann.ventils.features.dungeons;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.EarlyEnterCategory;
import dev.ethann.ventils.features.ChatListener;
import dev.ethann.ventils.features.HudFeature;
import dev.ethann.ventils.features.SessionListener;
import dev.ethann.ventils.features.TickListener;
import dev.ethann.ventils.features.dungeons.EarlyEnterZones.EarlyEnterZone;
import dev.ethann.ventils.features.dungeons.hud.PlayerPositionsHud;
import dev.ethann.ventils.utils.TextUtils;
import dev.ethann.ventils.utils.Titles;
import dev.ethann.ventils.utils.skyblock.Island;
import dev.ethann.ventils.utils.skyblock.LocationUtils;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EarlyEnterHelper extends HudFeature implements TickListener, ChatListener, SessionListener {
	public static final EarlyEnterHelper INSTANCE = new EarlyEnterHelper();

	private static final String HUD_TITLE = "§e§lPlayer Positions";
	private static final Pattern PLAYER_CHAT = Pattern.compile(
		"^(?:(?:Party|Co-op|Officer) > )?(?:\\[[^\\]]+] )?(\\w{1,16})(?: [^:]*)?: (.+)$"
	);
	private static final Pattern LEAP_CHAT = Pattern.compile("^You have teleported to (\\w{1,16})!$");
	private static final Pattern WORD_AT = Pattern.compile("\\bat\\b");
	private static final Pattern WORD_IN = Pattern.compile("\\bin\\b");
	private static final Pattern WORD_MID = Pattern.compile("\\bmid\\b");
	private static final Pattern WORD_SS = Pattern.compile("\\bss\\b");
	private static final Pattern WORD_HIGH = Pattern.compile("\\bhigh\\b");
	private static final Pattern WORD_LOW = Pattern.compile("\\blow\\b");
	private static final Pattern WORD_CORE = Pattern.compile("\\bcore\\b");
	private static final Pattern WORD_OUTSIDE = Pattern.compile("\\boutside\\b");
	private static final List<String> CHAT_ORDER = List.of(
		"outside_core",
		"high_ee2",
		"high_ee3",
		"low_ee2",
		"low_ee3",
		"core",
		"ss",
		"mid"
	);
	private static final List<List<String>> LEAP_ZONES = List.of(
		List.of("ss", "mid"),
		List.of("low_ee2", "high_ee2"),
		List.of("low_ee3", "high_ee3"),
		List.of("core", "outside_core")
	);
	private static final double SKIP_LEAP_RANGE = 10.0;

	private final PlayerPositionsHud playerPositionsHud = new PlayerPositionsHud();
	private final Map<String, Occupant> occupants = new LinkedHashMap<>();
	private final Set<String> dismissed = new HashSet<>();
	private boolean wasInDungeons;
	private boolean wasInBoss;
	private boolean wasGoldorActive;
	private int lastGoldorPhase = -1;

	private EarlyEnterHelper() {
	}

	@Override
	public String name() {
		return "Early Enter Helper";
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public PlayerPositionsHud hud() {
		return playerPositionsHud;
	}

	@Override
	public void onTick(Minecraft client) {
		boolean inDungeons = DungeonUtils.inDungeons();
		boolean inBoss = DungeonUtils.inBoss();
		if (wasInDungeons && !inDungeons) {
			clearOccupants();
			wasGoldorActive = false;
			lastGoldorPhase = -1;
		}
		if (!wasInBoss && inBoss) {
			clearOccupants();
			wasGoldorActive = false;
			lastGoldorPhase = -1;
		}
		wasInDungeons = inDungeons;
		wasInBoss = inBoss;

		EarlyEnterCategory cfg = config();
		if (!cfg.enabled || !inDetectionArea()) {
			return;
		}
		ClientLevel level = client.level;
		if (level == null || client.player == null) {
			return;
		}
		for (EarlyEnterZone zone : EarlyEnterZones.all()) {
			if (!cfg.zoneEnabled(zone.id()) || !inCapturePhase(zone) || occupants.containsKey(zone.id()) || dismissed.contains(zone.id())) {
				continue;
			}
			List<AbstractClientPlayer> inside = playersInZone(level, zone);
			if (!inside.isEmpty()) {
				recordOccupant(zone, closest(inside, zone).getGameProfile().name());
			}
		}
		boolean goldorActive = InactiveWaypoints.INSTANCE.goldorActive();
		int goldorPhase = InactiveWaypoints.INSTANCE.goldorPhase();
		if (cfg.showLeapReminder) {
			if (goldorActive && !wasGoldorActive) {
				showLeapReminder(0, client.player);
			} else if (goldorActive && goldorPhase > lastGoldorPhase) {
				showLeapReminder(goldorPhase, client.player);
			}
		}
		wasGoldorActive = goldorActive;
		lastGoldorPhase = goldorActive ? goldorPhase : -1;
	}

	@Override
	public void onGameChat(String stripped) {
		EarlyEnterCategory cfg = config();
		if (!cfg.enabled) {
			return;
		}
		Matcher leap = LEAP_CHAT.matcher(stripped);
		if (leap.matches()) {
			removeLeaped(leap.group(1));
			return;
		}
		if (!inDetectionArea()) {
			return;
		}
		Matcher matcher = PLAYER_CHAT.matcher(stripped);
		if (!matcher.matches()) {
			return;
		}
		String name = matcher.group(1);
		String body = matcher.group(2);
		EarlyEnterZone zone = zoneFromChat(body);
		if (zone == null || !cfg.zoneEnabled(zone.id())) {
			return;
		}
		recordOccupant(zone, name);
	}

	@Override
	public void onJoin() {
		reset();
	}

	@Override
	public void onDisconnect() {
		reset();
	}

	public boolean inDetectionArea() {
		if (LocationUtils.currentArea() == Island.SINGLEPLAYER) {
			return true;
		}
		return DungeonUtils.isFloor(7) && DungeonUtils.inBoss();
	}

	public List<String> visibleLines() {
		EarlyEnterCategory cfg = config();
		LocalPlayer self = Minecraft.getInstance().player;
		List<String> lines = new ArrayList<>();
		for (EarlyEnterZone zone : EarlyEnterZones.all()) {
			if (!cfg.zoneEnabled(zone.id()) || !inDisplayWindow(zone)) {
				continue;
			}
			Occupant occupant = occupants.get(zone.id());
			if (occupant == null) {
				continue;
			}
			if (self != null && (
				occupant.name().equalsIgnoreCase(self.getGameProfile().name())
					|| zone.contains(self.position())
			)) {
				continue;
			}
			if (lines.isEmpty()) {
				lines.add(HUD_TITLE);
			}
			lines.add(occupant.name() + " " + occupant.zone().label());
		}
		return lines;
	}

	public static List<String> editorPreview() {
		return List.of(HUD_TITLE, "veneir In Core!");
	}

	private void recordOccupant(EarlyEnterZone zone, String name) {
		if (!inCapturePhase(zone) || occupants.containsKey(zone.id()) || dismissed.contains(zone.id())) {
			return;
		}
		occupants.put(zone.id(), new Occupant(name, zone));
	}

	private void showLeapReminder(int phase, LocalPlayer self) {
		if (phase < 0 || phase >= LEAP_ZONES.size() || self == null) {
			return;
		}
		EarlyEnterCategory cfg = config();
		String selfName = self.getGameProfile().name();
		ClientLevel level = Minecraft.getInstance().level;
		for (String id : LEAP_ZONES.get(phase)) {
			if (!cfg.zoneEnabled(id) || dismissed.contains(id)) {
				continue;
			}
			Occupant occupant = occupants.get(id);
			if (occupant == null || occupant.name().equalsIgnoreCase(selfName)) {
				continue;
			}
			if (alreadyAtLeapTarget(self, occupant, level)) {
				continue;
			}
			Titles.show(TextUtils.leapTitle(cfg.leapReminderText, occupant.name()));
			return;
		}
	}

	private static boolean alreadyAtLeapTarget(LocalPlayer self, Occupant occupant, ClientLevel level) {
		if (occupant.zone().contains(self.position())) {
			return true;
		}
		Vec3 target = occupant.zone().center();
		if (level != null) {
			for (AbstractClientPlayer player : level.players()) {
				if (player.getGameProfile().name().equalsIgnoreCase(occupant.name())) {
					target = player.position();
					break;
				}
			}
		}
		return self.position().distanceToSqr(target) <= SKIP_LEAP_RANGE * SKIP_LEAP_RANGE;
	}

	private void removeLeaped(String name) {
		occupants.entrySet().removeIf(entry -> {
			if (!entry.getValue().name().equalsIgnoreCase(name)) {
				return false;
			}
			dismissed.add(entry.getKey());
			return true;
		});
	}

	private boolean inCapturePhase(EarlyEnterZone zone) {
		return InactiveWaypoints.INSTANCE.goldorActive() && zone.inCapturePhase(InactiveWaypoints.INSTANCE.goldorPhase());
	}

	private boolean inDisplayWindow(EarlyEnterZone zone) {
		return InactiveWaypoints.INSTANCE.goldorActive() && zone.inDisplayWindow(InactiveWaypoints.INSTANCE.goldorPhase());
	}

	private EarlyEnterZone zoneFromChat(String body) {
		String text = body.toLowerCase(Locale.ROOT);
		for (String id : CHAT_ORDER) {
			if (matchesChat(id, text)) {
				return zoneById(id);
			}
		}
		EarlyEnterCategory cfg = config();
		if (has(WORD_AT, text) && text.contains("ee2") && !qualifiedEe(text, "2")) {
			return firstFreeEe(cfg, "high_ee2", "low_ee2");
		}
		if (has(WORD_AT, text) && text.contains("ee3") && !qualifiedEe(text, "3")) {
			return firstFreeEe(cfg, "high_ee3", "low_ee3");
		}
		return null;
	}

	private static boolean qualifiedEe(String text, String n) {
		return has(WORD_HIGH, text) || has(WORD_LOW, text) || compact(text, "hee" + n) || compact(text, "lee" + n);
	}

	private EarlyEnterZone firstFreeEe(EarlyEnterCategory cfg, String highId, String lowId) {
		for (String id : List.of(highId, lowId)) {
			if (cfg.zoneEnabled(id) && !occupants.containsKey(id) && !dismissed.contains(id)) {
				return zoneById(id);
			}
		}
		return null;
	}

	private static EarlyEnterZone zoneById(String id) {
		for (EarlyEnterZone zone : EarlyEnterZones.all()) {
			if (zone.id().equals(id)) {
				return zone;
			}
		}
		return null;
	}

	private static boolean matchesChat(String id, String text) {
		return switch (id) {
			case "mid" -> has(WORD_AT, text) && has(WORD_MID, text);
			case "ss" -> has(WORD_AT, text) && has(WORD_SS, text);
			case "high_ee2" -> has(WORD_AT, text) && (highAnd(text, "ee2") || compact(text, "hee2"));
			case "low_ee2" -> has(WORD_AT, text) && (lowAnd(text, "ee2") || compact(text, "lee2"));
			case "high_ee3" -> has(WORD_AT, text) && (highAnd(text, "ee3") || compact(text, "hee3"));
			case "low_ee3" -> has(WORD_AT, text) && (lowAnd(text, "ee3") || compact(text, "lee3"));
			case "core" -> (has(WORD_AT, text) || has(WORD_IN, text)) && has(WORD_CORE, text) && !has(WORD_OUTSIDE, text);
			case "outside_core" -> has(WORD_OUTSIDE, text) && has(WORD_CORE, text);
			default -> false;
		};
	}

	private static boolean highAnd(String text, String token) {
		return has(WORD_HIGH, text) && text.contains(token);
	}

	private static boolean lowAnd(String text, String token) {
		return has(WORD_LOW, text) && text.contains(token);
	}

	private static boolean compact(String text, String token) {
		return text.contains(token);
	}

	private static boolean has(Pattern pattern, String text) {
		return pattern.matcher(text).find();
	}

	private static List<AbstractClientPlayer> playersInZone(ClientLevel level, EarlyEnterZone zone) {
		List<AbstractClientPlayer> inside = new ArrayList<>();
		for (AbstractClientPlayer player : level.players()) {
			if (player.isSpectator()) {
				continue;
			}
			if (zone.contains(player.position())) {
				inside.add(player);
			}
		}
		return inside;
	}

	private static AbstractClientPlayer closest(List<AbstractClientPlayer> players, EarlyEnterZone zone) {
		AbstractClientPlayer best = players.getFirst();
		double bestDist = best.position().distanceToSqr(zone.center());
		for (int i = 1; i < players.size(); i++) {
			AbstractClientPlayer player = players.get(i);
			double dist = player.position().distanceToSqr(zone.center());
			if (dist < bestDist) {
				bestDist = dist;
				best = player;
			}
		}
		return best;
	}

	private void reset() {
		clearOccupants();
		wasInDungeons = false;
		wasInBoss = false;
		wasGoldorActive = false;
		lastGoldorPhase = -1;
	}

	private void clearOccupants() {
		occupants.clear();
		dismissed.clear();
	}

	private static EarlyEnterCategory config() {
		return Ventils.CONFIG.getInstance().dungeons.f7m7.earlyEnter;
	}

	private record Occupant(String name, EarlyEnterZone zone) {
	}
}
