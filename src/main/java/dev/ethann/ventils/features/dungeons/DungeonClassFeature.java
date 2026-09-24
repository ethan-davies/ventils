package dev.ethann.ventils.features.dungeons;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.DungeonsCategory;
import dev.ethann.ventils.features.ChatFeature;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonClass;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DungeonClassFeature extends ChatFeature {
	public static final DungeonClassFeature INSTANCE = new DungeonClassFeature();
	private static final Pattern CLASS_REGEX = Pattern.compile("^You have selected the (\\w+) Dungeon Class!$");

	private DungeonClassFeature() {
	}

	@Override
	public String name() {
		return "Dungeon Class";
	}

	@Override
	public boolean isEnabled() {
		return Ventils.CONFIG.getInstance().dungeons.autoDetectClass;
	}

	@Override
	public void onGameChat(String stripped) {
		if (!isEnabled()) {
			return;
		}
		Matcher matcher = CLASS_REGEX.matcher(stripped);
		if (!matcher.matches()) {
			return;
		}
		DungeonClass parsed = DungeonClass.fromLabel(matcher.group(1));
		if (parsed == null) {
			return;
		}
		DungeonsCategory dungeons = Ventils.CONFIG.getInstance().dungeons;
		dungeons.dungeonClass = parsed;
		Ventils.CONFIG.saveToFile();
		Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(
			Component.literal("[VT] ").withStyle(ChatFormatting.AQUA)
				.append(Component.literal("Set role to " + parsed).withStyle(ChatFormatting.WHITE))
		);
	}
}
