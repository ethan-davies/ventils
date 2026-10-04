package dev.ethann.ventils.features.dailies;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.DailyTasksCategory;
import dev.ethann.ventils.features.Hud;
import dev.ethann.ventils.features.dailies.DailyTasksHelper.Line;
import dev.ethann.ventils.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class DailyTasksHud extends Hud {
	@Override
	public Identifier id() {
		return Ventils.id("daily_tasks");
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public boolean shouldDraw() {
		if (inInventory()) {
			return false;
		}
		return DailyTasksHelper.INSTANCE.showing() && !lines(false).isEmpty();
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
		return 10;
	}

	@Override
	public Component editorTitle() {
		return Component.literal("Daily Tasks HUD");
	}

	@Override
	public void savePosition(int x, int y) {
		DailyTasksCategory cfg = config();
		cfg.hudX = x;
		cfg.hudY = y;
		Ventils.CONFIG.saveToFile();
	}

	@Override
	public float scale() {
		return config().hudScale;
	}

	@Override
	public void saveScale(float scale) {
		config().hudScale = scale;
		Ventils.CONFIG.saveToFile();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, boolean example) {
		drawLines(graphics, font, x, y, texts(lines(example)));
	}

	@Override
	public int width(Font font, boolean example) {
		return linesWidth(font, texts(lines(example)));
	}

	@Override
	public int height(boolean example) {
		return linesHeight(texts(lines(example)));
	}

	public void renderOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (!isEnabled() || !DailyTasksHelper.INSTANCE.showing()) {
			return;
		}
		Font font = Minecraft.getInstance().font;
		renderScaled(graphics, font, x(), y(), scale(), false);
		Hit hit = hitAt(mouseX, mouseY);
		if (hit == null) {
			return;
		}
		graphics.setComponentTooltipForNextFrame(font, tooltip(hit), mouseX, mouseY);
	}

	public boolean mouseClicked(MouseButtonEvent event) {
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			return false;
		}
		if (!isEnabled() || !DailyTasksHelper.INSTANCE.showing()) {
			return false;
		}
		Hit hit = hitAt(event.x(), event.y());
		if (hit == null) {
			return false;
		}
		switch (hit.kind()) {
			case RESET -> DailyTasksHelper.INSTANCE.reset();
			case COMPLETE_ALL -> DailyTasksHelper.INSTANCE.completeAll();
			case TASK -> DailyTasksHelper.INSTANCE.complete(hit.task());
		}
		return true;
	}

	private Hit hitAt(double mouseX, double mouseY) {
		if (!inInventory()) {
			return null;
		}
		List<Line> rows = lines(false);
		Font font = Minecraft.getInstance().font;
		float scale = scale();
		float left = x();
		float top = y();
		float rowHeight = LINE_HEIGHT * scale;
		for (int i = 0; i < rows.size(); i++) {
			Line row = rows.get(i);
			if (row.task() == null && !row.actions()) {
				continue;
			}
			float rowTop = top + rowHeight * i;
			if (mouseY < rowTop || mouseY >= rowTop + rowHeight) {
				continue;
			}
			if (row.actions()) {
				float resetWidth = font.width(DailyTasksHelper.RESET_LABEL) * scale;
				float gap = font.width(" ") * scale;
				float completeLeft = left + resetWidth + gap;
				float completeWidth = font.width(DailyTasksHelper.COMPLETE_ALL_LABEL) * scale;
				if (mouseX >= left && mouseX <= left + resetWidth) {
					return new Hit(Kind.RESET, null);
				}
				if (mouseX >= completeLeft && mouseX <= completeLeft + completeWidth) {
					return new Hit(Kind.COMPLETE_ALL, null);
				}
				return null;
			}
			float rowWidth = font.width(TextUtils.strip(row.text())) * scale;
			if (mouseX >= left && mouseX <= left + rowWidth) {
				return new Hit(Kind.TASK, row.task());
			}
		}
		return null;
	}

	private static List<Component> tooltip(Hit hit) {
		return switch (hit.kind()) {
			case RESET -> List.of(
				Component.literal("Reset").withStyle(ChatFormatting.RED),
				Component.literal("Click to show today's tasks again").withStyle(ChatFormatting.GRAY)
			);
			case COMPLETE_ALL -> List.of(
				Component.literal("Complete All").withStyle(ChatFormatting.GREEN),
				Component.literal("Click to hide every task for today").withStyle(ChatFormatting.GRAY)
			);
			case TASK -> List.of(
				Component.literal(hit.task().toString()),
				Component.literal("Click to hide this task for today").withStyle(ChatFormatting.GRAY)
			);
		};
	}

	private enum Kind {
		TASK,
		RESET,
		COMPLETE_ALL
	}

	private record Hit(Kind kind, DailyTask task) {
	}

	private List<Line> lines(boolean example) {
		boolean inventory = !example && inInventory();
		return DailyTasksHelper.INSTANCE.lines(example, inventory);
	}

	private static List<String> texts(List<Line> rows) {
		List<String> texts = new ArrayList<>(rows.size());
		for (Line row : rows) {
			texts.add(row.text());
		}
		return texts;
	}

	private static boolean inInventory() {
		return Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen<?>;
	}

	private static DailyTasksCategory config() {
		return Ventils.CONFIG.getInstance().general.dailyTasks;
	}
}
