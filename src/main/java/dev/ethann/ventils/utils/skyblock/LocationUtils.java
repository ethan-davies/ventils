package dev.ethann.ventils.utils.skyblock;

import dev.ethann.ventils.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public final class LocationUtils {
	private static Island currentArea = Island.UNKNOWN;
	private static boolean inSkyblock;

	private LocationUtils() {
	}

	public static Island currentArea() {
		return currentArea;
	}

	public static boolean inSkyblock() {
		return inSkyblock;
	}

	public static boolean isCurrentArea(Island... areas) {
		if (currentArea == Island.SINGLEPLAYER) {
			return true;
		}
		for (Island area : areas) {
			if (currentArea == area) {
				return true;
			}
		}
		return false;
	}

	public static void reset() {
		Minecraft mc = Minecraft.getInstance();
		currentArea = mc.hasSingleplayerServer() ? Island.SINGLEPLAYER : Island.UNKNOWN;
		inSkyblock = false;
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) {
			reset();
			return;
		}

		if (mc.hasSingleplayerServer()) {
			currentArea = Island.SINGLEPLAYER;
			return;
		}

		Scoreboard scoreboard = mc.level.getScoreboard();
		Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (sidebar != null && "SBScoreboard".equals(sidebar.getName())) {
			inSkyblock = true;
		}

		if (currentArea != Island.UNKNOWN) {
			return;
		}

		ClientPacketListener connection = mc.getConnection();
		if (connection == null) {
			return;
		}

		for (PlayerInfo info : connection.getListedOnlinePlayers()) {
			Component displayName = info.getTabListDisplayName();
			if (displayName == null) {
				continue;
			}
			String line = displayName.getString();
			if (line.startsWith("Area: ") || line.startsWith("Dungeon: ")) {
				currentArea = Island.fromAreaLine(line);
				return;
			}
		}

		for (var team : scoreboard.getPlayerTeams()) {
			String text = TextUtils.strip(team.getPlayerPrefix().getString() + team.getPlayerSuffix().getString());
			if (text.startsWith("Area: ") || text.startsWith("Dungeon: ") || text.contains("The Catacombs")) {
				currentArea = Island.fromAreaLine(text.contains("The Catacombs") ? "Dungeon: Catacombs" : text);
				return;
			}
		}
	}
}
