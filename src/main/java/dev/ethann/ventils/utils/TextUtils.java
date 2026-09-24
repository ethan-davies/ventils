package dev.ethann.ventils.utils;

import dev.ethann.ventils.utils.skyblock.dungeon.DungeonClass;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.regex.Pattern;

public final class TextUtils {
	private static final Pattern AMPERSAND_COLOR = Pattern.compile("(?i)&(?=[0-9a-fk-orx])");

	private TextUtils() {
	}

	public static String strip(String text) {
		if (text == null) {
			return "";
		}
		String stripped = ChatFormatting.stripFormatting(text);
		return stripped == null ? text : stripped;
	}

	public static String colorCodes(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		return AMPERSAND_COLOR.matcher(text).replaceAll("§");
	}

	public static Component leapTitle(String template, String player) {
		String name = player == null ? "Player" : player;
		DungeonClass dungeonClass = DungeonUtils.classOf(name);
		String classLabel = dungeonClass != null ? dungeonClass.toString() : name;
		String format = template == null || template.isBlank() ? "&dLeap to {class}" : template;
		return Component.literal(colorCodes(
			format.replace("{class}", classLabel).replace("{player}", name)
		));
	}

	public static boolean containsIgnoreCase(String text, String... needles) {
		String haystack = text.toLowerCase();
		for (String needle : needles) {
			if (haystack.contains(needle.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
