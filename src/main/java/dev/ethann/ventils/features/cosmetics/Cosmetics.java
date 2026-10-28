package dev.ethann.ventils.features.cosmetics;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.CosmeticsCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class Cosmetics {
	private static int tooltipDepth;
	private static String cachedRaw;
	private static List<Run> cachedRuns = List.of();
	private static String cachedPlain = "";
	private static boolean cachedStyled;

	private Cosmetics() {
	}

	public static void beginTooltip() {
		tooltipDepth++;
	}

	public static void endTooltip() {
		if (tooltipDepth > 0) {
			tooltipDepth--;
		}
	}

	public static String replace(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		if (rankActive()) {
			input = hideRanks(input);
		}
		if (!customNameActive()) {
			return input;
		}
		String self = selfName();
		if (self.isEmpty() || !input.contains(self)) {
			return input;
		}
		String plain = cachedPlain;
		StringBuilder out = new StringBuilder(input.length());
		int index = 0;
		boolean replaced = false;
		while (index < input.length()) {
			if (input.startsWith(self, index) && bounded(input, index, self.length())) {
				out.append(plain);
				index += self.length();
				replaced = true;
			} else {
				int cp = input.codePointAt(index);
				out.appendCodePoint(cp);
				index += Character.charCount(cp);
			}
		}
		return replaced ? out.toString() : input;
	}

	public static FormattedText replace(FormattedText input) {
		if (!(input instanceof Component component)) {
			return input;
		}
		if (rankActive()) {
			Component stripped = hideRanks(component);
			if (!customNameActive()) {
				return stripped;
			}
			component = stripped;
		} else if (!customNameActive()) {
			return input;
		}
		String self = selfName();
		if (self.isEmpty() || !component.getString().contains(self)) {
			return component;
		}
		Flat flat = Flat.of(component);
		int[] starts = find(flat.codepoints, flat.count, self);
		if (starts == null) {
			return component;
		}
		return rebuild(flat, starts, self.codePointCount(0, self.length()));
	}

	public static FormattedCharSequence replace(FormattedCharSequence input) {
		if (input == null) {
			return null;
		}
		if (rankActive()) {
			input = hideRanks(input);
		}
		if (!customNameActive()) {
			return input;
		}
		String self = selfName();
		if (self.isEmpty()) {
			return input;
		}
		Flat flat = Flat.of(input);
		int[] starts = find(flat.codepoints, flat.count, self);
		if (starts == null) {
			return input;
		}
		int nameLength = self.codePointCount(0, self.length());
		return sink -> {
			int cursor = 0;
			for (int start : starts) {
				if (!emitRange(sink, flat, cursor, start)) {
					return false;
				}
				if (!emitReplacement(sink, styleAt(flat, start))) {
					return false;
				}
				cursor = start + nameLength;
			}
			return emitRange(sink, flat, cursor, flat.count);
		};
	}

	private static boolean rankActive() {
		return Ventils.CONFIG != null && config().hideRankPrefix;
	}

	private static boolean customNameActive() {
		if (Ventils.CONFIG == null) {
			return false;
		}
		CosmeticsCategory cfg = config();
		if (!cfg.showCustomName || (tooltipDepth > 0 && !cfg.showInLore)) {
			return false;
		}
		refresh();
		if (cachedPlain.isBlank()) {
			return false;
		}
		return cachedStyled || !cachedPlain.equals(selfName());
	}

	private static String hideRanks(String input) {
		if (input.indexOf('[') < 0) {
			return input;
		}
		int[] codepoints = input.codePoints().toArray();
		int[][] spans = rankSpans(codepoints, codepoints.length);
		if (spans == null) {
			return input;
		}
		StringBuilder out = new StringBuilder(input.length());
		int cursor = 0;
		for (int span = 0; span < spans[0].length; span++) {
			appendCodePoints(out, codepoints, cursor, spans[0][span]);
			int end = spans[1][span];
			String formatting = legacyFormatting(codepoints, end);
			if (!formatting.isEmpty() && (end >= codepoints.length || codepoints[end] != '§')) {
				out.append(formatting);
			}
			cursor = end;
		}
		appendCodePoints(out, codepoints, cursor, codepoints.length);
		return out.toString();
	}

	private static Component hideRanks(Component component) {
		if (component.getString().indexOf('[') < 0) {
			return component;
		}
		Flat flat = Flat.of(component);
		int[][] spans = rankSpans(flat.codepoints, flat.count);
		if (spans == null) {
			return component;
		}
		MutableComponent out = Component.empty();
		int cursor = 0;
		Style carry = Style.EMPTY;
		for (int span = 0; span < spans[0].length; span++) {
			appendRange(out, flat, cursor, spans[0][span], carry);
			carry = colorStyle(legacyColor(flat.codepoints, spans[1][span]));
			cursor = spans[1][span];
		}
		appendRange(out, flat, cursor, flat.count, carry);
		return out;
	}

	private static FormattedCharSequence hideRanks(FormattedCharSequence input) {
		Flat flat = Flat.of(input);
		int[][] spans = rankSpans(flat.codepoints, flat.count);
		if (spans == null) {
			return input;
		}
		return sink -> {
			int cursor = 0;
			Style carry = Style.EMPTY;
			for (int span = 0; span < spans[0].length; span++) {
				if (!emitRange(sink, flat, cursor, spans[0][span], carry)) {
					return false;
				}
				carry = colorStyle(legacyColor(flat.codepoints, spans[1][span]));
				cursor = spans[1][span];
			}
			return emitRange(sink, flat, cursor, flat.count, carry);
		};
	}

	private static int[][] rankSpans(int[] codepoints, int count) {
		int[] starts = new int[4];
		int[] ends = new int[4];
		int found = 0;
		for (int index = 0; index < count; index++) {
			if (codepoints[index] != '[') {
				continue;
			}
			int start = index;
			while (start >= 2 && codepoints[start - 2] == '§' && isLegacyCode(codepoints[start - 1])) {
				start -= 2;
			}
			if (start > 0 && isNameChar(codepoints[start - 1])) {
				continue;
			}
			int cursor = index + 1;
			StringBuilder word = new StringBuilder(8);
			while (cursor < count) {
				if (codepoints[cursor] == '§' && cursor + 1 < count && isLegacyCode(codepoints[cursor + 1])) {
					cursor += 2;
					continue;
				}
				int current = codepoints[cursor];
				if ((current >= 'A' && current <= 'Z') || (current >= 'a' && current <= 'z')) {
					word.append((char) current);
					cursor++;
					continue;
				}
				break;
			}
			int maxPlus = maxPluses(word.toString());
			if (maxPlus < 0) {
				continue;
			}
			int pluses = 0;
			while (pluses < maxPlus) {
				int look = cursor;
				while (look < count && codepoints[look] == '§' && look + 1 < count && isLegacyCode(codepoints[look + 1])) {
					look += 2;
				}
				if (look >= count || codepoints[look] != '+') {
					break;
				}
				pluses++;
				cursor = look + 1;
			}
			while (cursor < count && codepoints[cursor] == '§' && cursor + 1 < count && isLegacyCode(codepoints[cursor + 1])) {
				cursor += 2;
			}
			if (cursor >= count || codepoints[cursor] != ']') {
				continue;
			}
			int end = cursor + 1;
			while (end < count && codepoints[end] == '§' && end + 1 < count && isLegacyCode(codepoints[end + 1])) {
				end += 2;
			}
			if (end < count && codepoints[end] == ' ') {
				end++;
			}
			if (found == starts.length) {
				int[] grownStarts = new int[starts.length * 2];
				int[] grownEnds = new int[ends.length * 2];
				System.arraycopy(starts, 0, grownStarts, 0, starts.length);
				System.arraycopy(ends, 0, grownEnds, 0, ends.length);
				starts = grownStarts;
				ends = grownEnds;
			}
			starts[found] = start;
			ends[found] = end;
			found++;
			index = end - 1;
		}
		if (found == 0) {
			return null;
		}
		int[] trimmedStarts = new int[found];
		int[] trimmedEnds = new int[found];
		System.arraycopy(starts, 0, trimmedStarts, 0, found);
		System.arraycopy(ends, 0, trimmedEnds, 0, found);
		return new int[][]{trimmedStarts, trimmedEnds};
	}

	private static int maxPluses(String word) {
		return switch (word.toUpperCase(Locale.ROOT)) {
			case "VIP" -> 1;
			case "MVP" -> 2;
			case "YOUTUBE" -> 0;
			default -> -1;
		};
	}

	private static boolean isLegacyCode(int codepoint) {
		char code = Character.toLowerCase((char) codepoint);
		return (code >= '0' && code <= '9')
			|| (code >= 'a' && code <= 'f')
			|| code == 'k' || code == 'l' || code == 'm' || code == 'n' || code == 'o' || code == 'r';
	}

	private static char legacyColor(int[] codepoints, int end) {
		char color = 0;
		int index = 0;
		while (index + 1 < end) {
			if (codepoints[index] == '§' && isLegacyCode(codepoints[index + 1])) {
				char code = Character.toLowerCase((char) codepoints[index + 1]);
				if (code == 'r') {
					color = 0;
				} else if (isColorCode(code)) {
					color = code;
				}
				index += 2;
				continue;
			}
			index++;
		}
		return color;
	}

	private static String legacyFormatting(int[] codepoints, int end) {
		char color = 0;
		boolean obfuscated = false;
		boolean bold = false;
		boolean strikethrough = false;
		boolean underlined = false;
		boolean italic = false;
		int index = 0;
		while (index + 1 < end) {
			if (codepoints[index] == '§' && isLegacyCode(codepoints[index + 1])) {
				char code = Character.toLowerCase((char) codepoints[index + 1]);
				if (code == 'r') {
					color = 0;
					obfuscated = false;
					bold = false;
					strikethrough = false;
					underlined = false;
					italic = false;
				} else if (isColorCode(code)) {
					color = code;
					obfuscated = false;
					bold = false;
					strikethrough = false;
					underlined = false;
					italic = false;
				} else switch (code) {
					case 'k' -> obfuscated = true;
					case 'l' -> bold = true;
					case 'm' -> strikethrough = true;
					case 'n' -> underlined = true;
					case 'o' -> italic = true;
					default -> {
					}
				}
				index += 2;
				continue;
			}
			index++;
		}
		StringBuilder formatting = new StringBuilder();
		if (color != 0) {
			formatting.append('§').append(color);
		}
		if (obfuscated) {
			formatting.append("§k");
		}
		if (bold) {
			formatting.append("§l");
		}
		if (strikethrough) {
			formatting.append("§m");
		}
		if (underlined) {
			formatting.append("§n");
		}
		if (italic) {
			formatting.append("§o");
		}
		return formatting.toString();
	}

	private static boolean isColorCode(char code) {
		return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
	}

	private static Style colorStyle(char color) {
		if (color == 0) {
			return Style.EMPTY;
		}
		ChatFormatting formatting = ChatFormatting.getByCode(color);
		return formatting == null ? Style.EMPTY : Style.EMPTY.withColor(formatting);
	}

	private static void appendCodePoints(StringBuilder out, int[] codepoints, int from, int to) {
		for (int index = from; index < to; index++) {
			out.appendCodePoint(codepoints[index]);
		}
	}

	private static void refresh() {
		String raw = config().name == null ? "" : config().name;
		if (raw.equals(cachedRaw)) {
			return;
		}
		cachedRaw = raw;
		cachedRuns = parse(raw);
		StringBuilder plain = new StringBuilder();
		boolean styled = false;
		for (Run run : cachedRuns) {
			plain.append(run.text);
			styled |= !run.style.isEmpty();
		}
		cachedPlain = plain.toString();
		cachedStyled = styled;
	}

	private static Component rebuild(Flat flat, int[] starts, int nameLength) {
		MutableComponent out = Component.empty();
		int cursor = 0;
		for (int start : starts) {
			appendRange(out, flat, cursor, start);
			appendReplacement(out, styleAt(flat, start));
			cursor = start + nameLength;
		}
		appendRange(out, flat, cursor, flat.count);
		return out;
	}

	private static Style styleAt(Flat flat, int index) {
		Style style = flat.styles[index];
		return style == null ? Style.EMPTY : style;
	}

	private static void appendRange(MutableComponent out, Flat flat, int from, int to) {
		appendRange(out, flat, from, to, Style.EMPTY);
	}

	private static void appendRange(MutableComponent out, Flat flat, int from, int to, Style carry) {
		int index = from;
		while (index < to) {
			Style style = styleAt(flat, index).applyTo(carry);
			int end = index + 1;
			while (end < to && styleAt(flat, end).applyTo(carry).equals(style)) {
				end++;
			}
			out.append(Component.literal(new String(flat.codepoints, index, end - index)).setStyle(style));
			index = end;
		}
	}

	private static void appendReplacement(MutableComponent out, Style base) {
		for (Run run : cachedRuns) {
			out.append(Component.literal(run.text).setStyle(run.style.applyTo(base)));
		}
	}

	private static boolean emitRange(FormattedCharSink sink, Flat flat, int from, int to) {
		return emitRange(sink, flat, from, to, Style.EMPTY);
	}

	private static boolean emitRange(FormattedCharSink sink, Flat flat, int from, int to, Style carry) {
		for (int index = from; index < to; index++) {
			if (!sink.accept(0, styleAt(flat, index).applyTo(carry), flat.codepoints[index])) {
				return false;
			}
		}
		return true;
	}

	private static boolean emitReplacement(FormattedCharSink sink, Style base) {
		for (Run run : cachedRuns) {
			Style style = run.style.applyTo(base);
			int[] points = run.text.codePoints().toArray();
			for (int point : points) {
				if (!sink.accept(0, style, point)) {
					return false;
				}
			}
		}
		return true;
	}

	private static int[] find(int[] codepoints, int count, String name) {
		int nameLength = name.codePointCount(0, name.length());
		if (nameLength == 0 || count < nameLength) {
			return null;
		}
		int[] namePoints = name.codePoints().toArray();
		int[] starts = new int[4];
		int found = 0;
		for (int index = 0; index + nameLength <= count; index++) {
			if (!matches(codepoints, index, namePoints) || !bounded(codepoints, count, index, nameLength)) {
				continue;
			}
			if (found == starts.length) {
				int[] grown = new int[starts.length * 2];
				System.arraycopy(starts, 0, grown, 0, starts.length);
				starts = grown;
			}
			starts[found++] = index;
			index += nameLength - 1;
		}
		if (found == 0) {
			return null;
		}
		int[] trimmed = new int[found];
		System.arraycopy(starts, 0, trimmed, 0, found);
		return trimmed;
	}

	private static boolean matches(int[] codepoints, int start, int[] name) {
		for (int index = 0; index < name.length; index++) {
			if (codepoints[start + index] != name[index]) {
				return false;
			}
		}
		return true;
	}

	private static boolean bounded(String text, int start, int length) {
		int before = previousContent(text, start);
		if (before >= 0 && isNameChar(text.codePointAt(before))) {
			return false;
		}
		int after = start + length;
		return after >= text.length() || !isNameChar(text.codePointAt(after));
	}

	private static boolean bounded(int[] codepoints, int count, int start, int length) {
		int before = previousContent(codepoints, start);
		if (before >= 0 && isNameChar(codepoints[before])) {
			return false;
		}
		int after = start + length;
		return after >= count || !isNameChar(codepoints[after]);
	}

	private static int previousContent(String text, int start) {
		int index = start - 1;
		while (index >= 1 && text.charAt(index - 1) == '\u00A7') {
			index -= 2;
		}
		return index;
	}

	private static int previousContent(int[] codepoints, int start) {
		int index = start - 1;
		while (index >= 1 && codepoints[index - 1] == '\u00A7') {
			index -= 2;
		}
		return index;
	}

	private static boolean isColor(ChatFormatting formatting) {
		return switch (formatting) {
			case OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET -> false;
			default -> true;
		};
	}

	private static boolean isNameChar(int codepoint) {
		return (codepoint >= 'a' && codepoint <= 'z')
			|| (codepoint >= 'A' && codepoint <= 'Z')
			|| (codepoint >= '0' && codepoint <= '9')
			|| codepoint == '_';
	}

	private static List<Run> parse(String input) {
		List<Run> runs = new ArrayList<>();
		Style style = Style.EMPTY;
		StringBuilder text = new StringBuilder();
		int index = 0;
		while (index < input.length()) {
			char current = input.charAt(index);
			if ((current == '&' || current == '\u00A7') && index + 1 < input.length()) {
				char code = input.charAt(index + 1);
				if (code == '#' && index + 7 < input.length() && isHex(input, index + 2)) {
					flush(runs, text, style);
					int rgb = Integer.parseInt(input.substring(index + 2, index + 8), 16);
					style = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
					index += 8;
					continue;
				}
				ChatFormatting formatting = ChatFormatting.getByCode(Character.toLowerCase(code));
				if (formatting != null) {
					flush(runs, text, style);
					if (formatting == ChatFormatting.RESET) {
						style = Style.EMPTY;
					} else if (isColor(formatting)) {
						style = Style.EMPTY.withColor(formatting);
					} else {
						style = style.applyFormat(formatting);
					}
					index += 2;
					continue;
				}
			}
			text.append(current);
			index++;
		}
		flush(runs, text, style);
		return List.copyOf(runs);
	}

	private static void flush(List<Run> runs, StringBuilder text, Style style) {
		if (text.isEmpty()) {
			return;
		}
		runs.add(new Run(text.toString(), style));
		text.setLength(0);
	}

	private static boolean isHex(String text, int start) {
		for (int index = start; index < start + 6; index++) {
			char current = text.charAt(index);
			boolean hex = (current >= '0' && current <= '9')
				|| (current >= 'a' && current <= 'f')
				|| (current >= 'A' && current <= 'F');
			if (!hex) {
				return false;
			}
		}
		return true;
	}

	private static String selfName() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) {
			String name = minecraft.player.getGameProfile().name();
			if (name != null && !name.isEmpty()) {
				return name;
			}
		}
		return minecraft.getUser().getName();
	}

	private static CosmeticsCategory config() {
		CosmeticsCategory cosmetics = Ventils.CONFIG.getInstance().cosmetics;
		if (cosmetics == null) {
			cosmetics = new CosmeticsCategory();
			Ventils.CONFIG.getInstance().cosmetics = cosmetics;
		}
		return cosmetics;
	}

	private record Run(String text, Style style) {
	}

	private static final class Flat {
		private final int[] codepoints;
		private final Style[] styles;
		private final int count;

		private Flat(int[] codepoints, Style[] styles, int count) {
			this.codepoints = codepoints;
			this.styles = styles;
			this.count = count;
		}

		private static Flat of(Component component) {
			List<Integer> points = new ArrayList<>();
			List<Style> styles = new ArrayList<>();
			component.visit((style, text) -> {
				text.codePoints().forEach(codepoint -> {
					points.add(codepoint);
					styles.add(style);
				});
				return Optional.empty();
			}, Style.EMPTY);
			return from(points, styles);
		}

		private static Flat of(FormattedCharSequence sequence) {
			List<Integer> points = new ArrayList<>();
			List<Style> styles = new ArrayList<>();
			sequence.accept((index, style, codepoint) -> {
				points.add(codepoint);
				styles.add(style);
				return true;
			});
			return from(points, styles);
		}

		private static Flat from(List<Integer> points, List<Style> styles) {
			int count = points.size();
			int[] codepoints = new int[count];
			Style[] styleArray = new Style[count];
			for (int index = 0; index < count; index++) {
				codepoints[index] = points.get(index);
				styleArray[index] = styles.get(index);
			}
			return new Flat(codepoints, styleArray, count);
		}
	}
}
