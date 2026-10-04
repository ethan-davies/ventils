package dev.ethann.ventils.features;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.List;

public abstract class Hud {
	protected static final int LINE_HEIGHT = 9;
	public static final float MIN_SCALE = 0.5f;
	public static final float MAX_SCALE = 3f;
	public static final float SCALE_STEP = 0.1f;

	public abstract Identifier id();

	public abstract boolean isEnabled();

	public abstract int x();

	public abstract int y();

	public abstract int defaultX();

	public abstract int defaultY();

	public abstract Component editorTitle();

	public abstract void savePosition(int x, int y);

	public abstract float scale();

	public float defaultScale() {
		return 1f;
	}

	public abstract void saveScale(float scale);

	public abstract void render(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean example);

	public final void renderScaled(GuiGraphicsExtractor graphics, Font font, int x, int y, float scale, boolean example) {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		try {
			pose.translate(x, y);
			pose.scale(scale, scale);
			render(graphics, font, 0, 0, example);
		} finally {
			pose.popMatrix();
		}
	}

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
		renderScaled(graphics, mc.font, x(), y(), scale(), false);
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
