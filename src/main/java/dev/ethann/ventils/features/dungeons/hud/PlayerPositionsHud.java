package dev.ethann.ventils.features.dungeons.hud;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.EarlyEnterCategory;
import dev.ethann.ventils.features.Hud;
import dev.ethann.ventils.features.dungeons.EarlyEnterHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class PlayerPositionsHud extends Hud {
	@Override
	public Identifier id() {
		return Ventils.id("early_enter");
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public boolean shouldDraw() {
		return EarlyEnterHelper.INSTANCE.inDetectionArea() && !EarlyEnterHelper.INSTANCE.visibleLines().isEmpty();
	}

	@Override
	public int x() {
		return config().hudX;
	}

	@Override
	public int y() {
		return config().hudY;
	}

	@Override
	public int defaultX() {
		return 10;
	}

	@Override
	public int defaultY() {
		return 60;
	}

	@Override
	public Component editorTitle() {
		return Component.literal("Player Positions HUD");
	}

	@Override
	public void savePosition(int x, int y) {
		EarlyEnterCategory cfg = config();
		cfg.hudX = x;
		cfg.hudY = y;
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
		return example ? EarlyEnterHelper.editorPreview() : EarlyEnterHelper.INSTANCE.visibleLines();
	}

	private static EarlyEnterCategory config() {
		return Ventils.CONFIG.getInstance().dungeons.earlyEnter;
	}
}
