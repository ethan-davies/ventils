package dev.ethann.ventils.features.dungeons;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.LeapMessagesCategory;
import dev.ethann.ventils.features.SessionListener;
import dev.ethann.ventils.features.TickFeature;
import dev.ethann.ventils.features.WorldRenderListener;
import dev.ethann.ventils.features.dungeons.EarlyEnterZones.EarlyEnterZone;
import dev.ethann.ventils.render.WorldRenderQueue;
import dev.ethann.ventils.utils.skyblock.Island;
import dev.ethann.ventils.utils.skyblock.LocationUtils;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public final class LeapMessages extends TickFeature implements SessionListener, WorldRenderListener {
	public static final LeapMessages INSTANCE = new LeapMessages();

	private final Set<String> announced = new HashSet<>();
	private final Set<String> inside = new HashSet<>();
	private boolean wasInDungeons;
	private boolean wasInBoss;

	private LeapMessages() {
	}

	@Override
	public String name() {
		return "Leap Messages";
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public void onTick(Minecraft client) {
		boolean inDungeons = DungeonUtils.inDungeons();
		boolean inBoss = DungeonUtils.inBoss();
		if (wasInDungeons && !inDungeons) {
			clearAnnounced();
		}
		if (!wasInBoss && inBoss) {
			clearAnnounced();
		}
		wasInDungeons = inDungeons;
		wasInBoss = inBoss;

		LeapMessagesCategory cfg = config();
		if (!cfg.enabled || !inDetectionArea()) {
			inside.clear();
			return;
		}
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}
		Vec3 pos = player.position();
		Set<String> now = new HashSet<>();
		for (EarlyEnterZone zone : EarlyEnterZones.all()) {
			if (!cfg.zoneEnabled(zone.id()) || !zone.contains(pos)) {
				continue;
			}
			now.add(zone.id());
			if (!inside.contains(zone.id()) && announced.add(zone.id())) {
				player.connection.sendCommand("pc " + zone.label());
			}
		}
		inside.clear();
		inside.addAll(now);
	}

	@Override
	public void onJoin() {
		reset();
	}

	@Override
	public void onDisconnect() {
		reset();
	}

	@Override
	public void onWorldRender() {
		LeapMessagesCategory cfg = config();
		if (!cfg.boxes.enabled || !inDetectionArea()) {
			return;
		}
		Color color = cfg.boxes.color.getEffectiveColour();
		for (EarlyEnterZone zone : EarlyEnterZones.all()) {
			if (!cfg.zoneEnabled(zone.id()) || zone.box() == null) {
				continue;
			}
			WorldRenderQueue.box(zone.box(), color, true, cfg.boxes.fill);
		}
	}

	private static boolean inDetectionArea() {
		if (LocationUtils.currentArea() == Island.SINGLEPLAYER) {
			return true;
		}
		return DungeonUtils.isFloor(7) && DungeonUtils.inBoss();
	}

	private void reset() {
		clearAnnounced();
		wasInDungeons = false;
		wasInBoss = false;
	}

	private void clearAnnounced() {
		announced.clear();
		inside.clear();
	}

	private static LeapMessagesCategory config() {
		return Ventils.CONFIG.getInstance().dungeons.f7m7.leapMessages;
	}
}
