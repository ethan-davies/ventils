package dev.ethann.ventils.features.dungeons;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.PlatformHighlightCategory;
import dev.ethann.ventils.features.WorldRenderFeature;
import dev.ethann.ventils.render.WorldRenderQueue;
import dev.ethann.ventils.utils.skyblock.Island;
import dev.ethann.ventils.utils.skyblock.LocationUtils;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import net.minecraft.world.phys.AABB;

public final class PlatformHighlight extends WorldRenderFeature {
	public static final PlatformHighlight INSTANCE = new PlatformHighlight();

	private static final AABB BOX = new AABB(53.0, 63.0, 113.0, 56.0, 64.0, 116.0);

	private PlatformHighlight() {
	}

	@Override
	public String name() {
		return "Platform Highlight";
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public void onWorldRender() {
		if (!isEnabled() || !inDetectionArea()) {
			return;
		}
		PlatformHighlightCategory cfg = config();
		WorldRenderQueue.box(BOX, cfg.color.getEffectiveColour(), true, cfg.fill);
	}

	private static boolean inDetectionArea() {
		if (LocationUtils.currentArea() == Island.SINGLEPLAYER) {
			return true;
		}
		return DungeonUtils.isFloor(7) && DungeonUtils.inBoss();
	}

	private static PlatformHighlightCategory config() {
		return Ventils.CONFIG.getInstance().dungeons.platformHighlight;
	}
}
