package dev.ethann.ventils.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class Titles {
	private Titles() {
	}

	public static void show(Component title) {
		Minecraft mc = Minecraft.getInstance();
		mc.gui.hud.setTimes(0, 40, 10);
		mc.gui.hud.setTitle(title);
	}

	public static void show(String text, ChatFormatting color) {
		show(Component.literal(text).withStyle(color));
	}
}
