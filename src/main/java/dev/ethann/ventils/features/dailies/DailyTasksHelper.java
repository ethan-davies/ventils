package dev.ethann.ventils.features.dailies;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.DailyTasksCategory;
import dev.ethann.ventils.features.Hud;
import dev.ethann.ventils.features.HudFeature;
import dev.ethann.ventils.utils.skyblock.Island;
import dev.ethann.ventils.utils.skyblock.LocationUtils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class DailyTasksHelper extends HudFeature {
	public static final DailyTasksHelper INSTANCE = new DailyTasksHelper();
	private static final String TITLE = "§e§lDaily Tasks";
	static final String RESET_LABEL = "[Reset]";
	static final String COMPLETE_ALL_LABEL = "[Complete All]";
	private static final String ACTIONS = "§c" + RESET_LABEL + " §a" + COMPLETE_ALL_LABEL;

	private final DailyTasksHud hud = new DailyTasksHud();

	private DailyTasksHelper() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof AbstractContainerScreen<?>)) {
				return;
			}
			ScreenEvents.afterForeground(screen).register((current, graphics, mouseX, mouseY, tickProgress) -> hud.renderOverlay(graphics, mouseX, mouseY));
			ScreenMouseEvents.allowMouseClick(screen).register((current, event) -> !hud.mouseClicked(event));
		});
	}

	@Override
	public String name() {
		return "Daily Tasks Helper";
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public Hud hud() {
		return hud;
	}

	public boolean showing() {
		return LocationUtils.inSkyblock() || LocationUtils.currentArea() == Island.SINGLEPLAYER;
	}

	public List<Line> lines(boolean example, boolean inventory) {
		rollDay();
		List<DailyTask> selected = tasks();
		if (selected.isEmpty()) {
			if (!example) {
				return List.of();
			}
			return List.of(new Line(TITLE, null, false), new Line("§7No tasks selected", null, false));
		}

		List<Line> rows = new ArrayList<>();
		rows.add(new Line(TITLE, null, false));
		List<DailyTask> done = completed();
		int shown = 0;
		for (DailyTask task : selected) {
			if (task == null || (!example && done.contains(task))) {
				continue;
			}
			rows.add(new Line("§f" + task, task, false));
			shown++;
		}
		if (inventory) {
			rows.add(new Line(ACTIONS, null, true));
		} else if (shown == 0) {
			return List.of();
		}
		return rows;
	}

	public void complete(DailyTask task) {
		if (task == null) {
			return;
		}
		rollDay();
		List<DailyTask> done = completed();
		if (!done.contains(task)) {
			done.add(task);
		}
		config().completedDay = today();
		Ventils.CONFIG.saveToFile();
	}

	public void reset() {
		completed().clear();
		config().completedDay = today();
		Ventils.CONFIG.saveToFile();
	}

	public void completeAll() {
		rollDay();
		List<DailyTask> done = completed();
		for (DailyTask task : tasks()) {
			if (task != null && !done.contains(task)) {
				done.add(task);
			}
		}
		config().completedDay = today();
		Ventils.CONFIG.saveToFile();
	}

	private void rollDay() {
		DailyTasksCategory cfg = config();
		String today = today();
		if (today.equals(cfg.completedDay)) {
			return;
		}
		completed().clear();
		cfg.completedDay = today;
		Ventils.CONFIG.saveToFile();
	}

	private static List<DailyTask> tasks() {
		List<DailyTask> tasks = config().tasks;
		if (tasks == null) {
			tasks = new ArrayList<>();
			config().tasks = tasks;
		}
		return tasks;
	}

	private static List<DailyTask> completed() {
		List<DailyTask> completed = config().completed;
		if (completed == null) {
			completed = new ArrayList<>();
			config().completed = completed;
		}
		return completed;
	}

	private static String today() {
		return LocalDate.now(ZoneOffset.UTC).toString();
	}

	private static DailyTasksCategory config() {
		return Ventils.CONFIG.getInstance().general.dailyTasks;
	}

	public record Line(String text, DailyTask task, boolean actions) {
	}
}
