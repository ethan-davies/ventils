package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class GeneralCategory {
	@Expose
	@Accordion
	@ConfigOption(name = "Daily Tasks Helper", desc = "Tracks selected daily tasks across Skyblock.")
	public DailyTasksCategory dailyTasks = new DailyTasksCategory();
}
