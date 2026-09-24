package dev.ethann.ventils.features;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.Color;

public final class HudEditorScreen extends Screen {
	private final Screen parent;
	private final Hud hud;
	private int hudX;
	private int hudY;
	private boolean dragging;
	private double dragOffsetX;
	private double dragOffsetY;

	public HudEditorScreen(Screen parent, Hud hud) {
		super(hud.editorTitle());
		this.parent = parent;
		this.hud = hud;
		this.hudX = hud.x();
		this.hudY = hud.y();
	}

	public static boolean isEditing(Hud hud) {
		Screen screen = Minecraft.getInstance().gui.screen();
		return screen instanceof HudEditorScreen editor && editor.hud == hud;
	}

	public static void open(Hud hud) {
		open(Minecraft.getInstance().gui.screen(), hud);
	}

	public static void open(Screen parent, Hud hud) {
		Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(new HudEditorScreen(parent, hud)));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		hud.render(graphics, font, hudX, hudY, true);
		graphics.centeredText(font, "Right Click To Reset Position", width / 2, height / 2, Color.GRAY.getRGB());
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
			hudX = hud.defaultX();
			hudY = hud.defaultY();
			return true;
		}
		if (click.button() == InputConstants.MOUSE_BUTTON_LEFT && hitboxContains(click.x(), click.y())) {
			dragging = true;
			dragOffsetX = click.x() - hudX;
			dragOffsetY = click.y() - hudY;
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
		if (dragging && click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
			int hudWidth = hud.width(font, true);
			int hudHeight = hud.height(true);
			hudX = (int) Math.clamp(click.x() - dragOffsetX, 0, Math.max(0, width - hudWidth));
			hudY = (int) Math.clamp(click.y() - dragOffsetY, 0, Math.max(0, height - hudHeight));
			return true;
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		dragging = false;
		return super.mouseReleased(click);
	}

	@Override
	public void onClose() {
		hud.savePosition(hudX, hudY);
		if (minecraft != null) {
			minecraft.gui.setScreen(parent);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private boolean hitboxContains(double mouseX, double mouseY) {
		int hudWidth = hud.width(font, true);
		int hudHeight = hud.height(true);
		return mouseX >= hudX && mouseX <= hudX + hudWidth && mouseY >= hudY && mouseY <= hudY + hudHeight;
	}
}
