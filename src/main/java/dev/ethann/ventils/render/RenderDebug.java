package dev.ethann.ventils.render;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.utils.TextUtils;
import dev.ethann.ventils.utils.Titles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

public final class RenderDebug {
	private static final Color MARKER_COLOR = new Color(255, 255, 85, 230);
	private static BlockPos marker;
	private static BlockPos box;

	private RenderDebug() {
	}

	public static int toggleAtPlayer() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return 0;
		}
		BlockPos pos = mc.player.blockPosition();
		if (pos.equals(marker)) {
			marker = null;
			tell(mc, "Debug marker cleared");
			return 1;
		}
		marker = pos.immutable();
		tell(mc, "Debug marker at " + marker.getX() + ", " + marker.getY() + ", " + marker.getZ());
		return 1;
	}

	public static int showTitle() {
		Minecraft mc = Minecraft.getInstance();
		String name = mc.player == null ? "Player" : mc.player.getGameProfile().name();
		Titles.show(TextUtils.leapTitle(
			Ventils.CONFIG.getInstance().dungeons.earlyEnter.leapReminderText,
			name
		));
		tell(mc, "Debug title: Leap to " + name);
		return 1;
	}

	public static int toggleBox() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return 0;
		}
		BlockPos pos = mc.player.blockPosition();
		if (pos.equals(box)) {
			box = null;
			tell(mc, "Debug box cleared");
			return 1;
		}
		box = pos.immutable();
		tell(mc, "Debug box at " + box.getX() + ", " + box.getY() + ", " + box.getZ());
		return 1;
	}

	public static void extract() {
		if (marker != null) {
			WorldRenderQueue.box(new AABB(marker), MARKER_COLOR, false);
			WorldRenderQueue.tracer(Vec3.atCenterOf(marker), MARKER_COLOR, false);
			WorldRenderQueue.text("debug", Vec3.atCenterOf(marker).add(0, 1.75, 0), 2.25f, false);
		}
		if (box != null) {
			WorldRenderQueue.box(new AABB(box).inflate(1), leapBoxColor(), true, leapBoxFill());
		}
	}

	private static Color leapBoxColor() {
		return Ventils.CONFIG.getInstance().dungeons.leapMessages.boxes.color.getEffectiveColour();
	}

	private static boolean leapBoxFill() {
		return Ventils.CONFIG.getInstance().dungeons.leapMessages.boxes.fill;
	}

	private static void tell(Minecraft mc, String message) {
		mc.gui.hud.getChat().addClientSystemMessage(
			Component.literal("[VT] ").withStyle(ChatFormatting.AQUA)
				.append(Component.literal(message).withStyle(ChatFormatting.WHITE))
		);
	}
}
