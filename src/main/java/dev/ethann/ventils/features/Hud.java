package dev.ethann.ventils.features;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public abstract class Hud {
	protected static final int LINE_HEIGHT = 9;

	public abstract Identifier id();

	public abstract boolean isEnabled();

	public abstract int x();

	public abstract int y();

	public abstract int defaultX();

	public abstract int defaultY();

	public abstract Component editorTitle();

	public abstract void savePosition(int x, int y);

	public abstract void render(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean example);

	public abstract int width(Font font, boolean example);

	public abstract int height(boolean example);

	public boolean shouldDraw() {
		return true;
	}

	public void renderLive(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!isEnabled() || HudEditorScreen.isEditing(this) || !shouldDraw()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		render(graphics, mc.font, x(), y(), false);
	}

	protected static void drawLines(GuiGraphicsExtractor graphics, Font font, int x, int y, List<String> lines) {
		for (int i = 0; i < lines.size(); i++) {
			graphics.text(font, lines.get(i), x, y + LINE_HEIGHT * i, 0xFFFFFFFF, true);
		}
	}

	protected static int linesWidth(Font font, List<String> lines) {
		int width = 0;
		for (String line : lines) {
			width = Math.max(width, font.width(line));
		}
		return width;
	}

	protected static int linesHeight(List<String> lines) {
		return LINE_HEIGHT * lines.size();
	}
}
