package dev.ethann.ventils.utils.skyblock.dungeon;

import dev.ethann.ventils.utils.skyblock.Island;
import dev.ethann.ventils.utils.skyblock.LocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class DungeonUtils {
	private DungeonUtils() {
	}

	public static boolean inDungeons() {
		return LocationUtils.isCurrentArea(Island.DUNGEON);
	}

	public static boolean inBoss() {
		return DungeonListener.inBoss();
	}

	public static Floor floor() {
		return DungeonListener.floor();
	}

	public static DungeonClass classOf(String name) {
		return DungeonListener.classOf(name);
	}

	public static boolean isFloor(int... options) {
		Floor current = floor();
		if (current == null) {
			return false;
		}
		for (int option : options) {
			if (current.floorNumber() == option) {
				return true;
			}
		}
		return false;
	}

	public static M7Phases getF7Phase() {
		if ((!isFloor(7) || !inBoss()) && !LocationUtils.isCurrentArea(Island.SINGLEPLAYER)) {
			return M7Phases.UNKNOWN;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return M7Phases.UNKNOWN;
		}
		double y = player.getY();
		if (y > 210) {
			return M7Phases.P1;
		}
		if (y > 155) {
			return M7Phases.P2;
		}
		if (y > 100) {
			return M7Phases.P3;
		}
		if (y > 45) {
			return M7Phases.P4;
		}
		return M7Phases.P5;
	}

	public static void tick() {
		LocationUtils.tick();
		DungeonListener.tick();
	}

	public static void reset() {
		LocationUtils.reset();
		DungeonListener.reset();
	}
}
