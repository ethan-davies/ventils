package dev.ethann.ventils.utils.skyblock.dungeon;

import dev.ethann.ventils.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DungeonListener {
	private static final Pattern FLOOR_REGEX = Pattern.compile("The Catacombs \\((\\w+)\\)$");
	private static final Pattern CLASS_TAG = Pattern.compile("\\[([ABHMT])]\\s+(\\w{1,16})");

	private static Floor floor;
	private static boolean inBoss;
	private static final Map<String, DungeonClass> classes = new HashMap<>();

	private DungeonListener() {
	}

	public static Floor floor() {
		return floor;
	}

	public static boolean inBoss() {
		return inBoss;
	}

	public static DungeonClass classOf(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		return classes.get(name);
	}

	public static void reset() {
		floor = null;
		inBoss = false;
		classes.clear();
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			reset();
			return;
		}

		if (!DungeonUtils.inDungeons()) {
			inBoss = false;
			classes.clear();
			return;
		}

		scanFloor(mc.level.getScoreboard());
		scanClasses(mc);
		boolean newInBoss = isInBossRoom(mc.player);
		if (newInBoss != inBoss) {
			inBoss = newInBoss;
		}
	}

	private static void scanFloor(Scoreboard scoreboard) {
		if (floor != null) {
			return;
		}
		for (PlayerTeam team : scoreboard.getPlayerTeams()) {
			String text = TextUtils.strip(team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString());
			Matcher matcher = FLOOR_REGEX.matcher(text);
			if (matcher.find()) {
				floor = Floor.fromScoreboard(matcher.group(1));
				return;
			}
		}
	}

	private static void scanClasses(Minecraft mc) {
		Map<String, DungeonClass> next = new HashMap<>();
		for (PlayerTeam team : mc.level.getScoreboard().getPlayerTeams()) {
			collectClass(next, TextUtils.strip(team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString()));
		}
		ClientPacketListener connection = mc.getConnection();
		if (connection != null) {
			for (PlayerInfo info : connection.getListedOnlinePlayers()) {
				Component displayName = info.getTabListDisplayName();
				if (displayName != null) {
					collectClass(next, TextUtils.strip(displayName.getString()));
				}
			}
		}
		classes.clear();
		classes.putAll(next);
	}

	private static void collectClass(Map<String, DungeonClass> into, String text) {
		Matcher matcher = CLASS_TAG.matcher(text);
		while (matcher.find()) {
			DungeonClass dungeonClass = DungeonClass.fromLetter(matcher.group(1).charAt(0));
			if (dungeonClass != null) {
				into.put(matcher.group(2), dungeonClass);
			}
		}
	}

	private static boolean isInBossRoom(LocalPlayer player) {
		if (floor == null) {
			return false;
		}
		double x = player.getX();
		double z = player.getZ();
		return switch (floor.floorNumber()) {
			case 1 -> x > -71 && z > -39;
			case 2, 3, 4 -> x > -39 && z > -39;
			case 5, 6 -> x > -39 && z > -7;
			case 7 -> x > -7 && z > -7;
			default -> false;
		};
	}
}
