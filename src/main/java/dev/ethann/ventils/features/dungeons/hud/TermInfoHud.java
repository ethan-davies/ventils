package dev.ethann.ventils.features.dungeons.hud;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.F7RolesCategory;
import dev.ethann.ventils.features.Hud;
import dev.ethann.ventils.features.dungeons.GoldorWaypoints.WaypointRole;
import dev.ethann.ventils.features.dungeons.InactiveWaypoints;
import dev.ethann.ventils.features.dungeons.InactiveWaypoints.HudJob;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class TermInfoHud extends Hud {
	private static final String HUD_TITLE = "§e§lTerminals";
	private static final List<HudJob> EDITOR = List.of(
		new HudJob("Terminal #1", WaypointRole.REQUIRED, false),
		new HudJob("SS", WaypointRole.STACKED, false),
		new HudJob("Terminal #3", WaypointRole.OPTIONAL, false),
		new HudJob("Left Lever", WaypointRole.REQUIRED, true)
	);

	@Override
	public Identifier id() {
		return Ventils.id("term_info");
	}

	@Override
	public boolean isEnabled() {
		F7RolesCategory cfg = config();
		return cfg.termInfoHud && cfg.inactiveWaypoints;
	}

	@Override
	public boolean shouldDraw() {
		return DungeonUtils.inBoss() && InactiveWaypoints.INSTANCE.goldorActive() && !InactiveWaypoints.INSTANCE.hudJobs().isEmpty();
	}

	@Override
	public int x() {
		return config().termInfoX;
	}

	@Override
	public int y() {
		return config().termInfoY;
	}

	@Override
	public int defaultX() {
		return 10;
	}

	@Override
	public int defaultY() {
		return 10;
	}

	@Override
	public Component editorTitle() {
		return Component.literal("Term Info HUD");
	}

	@Override
	public void savePosition(int x, int y) {
		F7RolesCategory roles = config();
		roles.termInfoX = x;
		roles.termInfoY = y;
		Ventils.CONFIG.saveToFile();
	}

	@Override
	public float scale() {
		return config().termInfoScale;
	}

	@Override
	public void saveScale(float scale) {
		config().termInfoScale = scale;
		Ventils.CONFIG.saveToFile();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean example) {
		drawLines(graphics, font, x, y, lines(example));
	}

	@Override
	public int width(Font font, boolean example) {
		return linesWidth(font, lines(example));
	}

	@Override
	public int height(boolean example) {
		return linesHeight(lines(example));
	}

	private static List<String> lines(boolean example) {
		List<HudJob> jobs = example ? EDITOR : InactiveWaypoints.INSTANCE.hudJobs();
		List<String> lines = new ArrayList<>();
		lines.add(HUD_TITLE);
		for (HudJob job : jobs) {
			lines.add(jobColor(job) + job.name());
		}
		return lines;
	}

	private static String jobColor(HudJob job) {
		if (job.done()) {
			return "§a";
		}
		return switch (job.role()) {
			case REQUIRED -> "§c";
			case STACKED -> "§e";
			case OPTIONAL -> "§7";
		};
	}

	private static F7RolesCategory config() {
		return Ventils.CONFIG.getInstance().dungeons.f7m7.f7Roles;
	}
}
