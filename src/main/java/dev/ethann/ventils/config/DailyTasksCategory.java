package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import dev.ethann.ventils.features.HudEditorScreen;
import dev.ethann.ventils.features.dailies.DailyTask;
import dev.ethann.ventils.features.dailies.DailyTasksHelper;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.List;

public class DailyTasksCategory {
	@Expose
	@ConfigOption(name = "Enabled", desc = "Shows the daily task list across Skyblock.")
	@ConfigEditorBoolean
	public boolean enabled = true;

	@Expose
	@ConfigOption(name = "Tasks", desc = "Select which daily tasks to show. Drag to change the order.")
	@ConfigEditorDraggableList
	public List<DailyTask> tasks = new ArrayList<>(List.of(DailyTask.values()));

	@ConfigOption(name = "Edit HUD", desc = "Opens a drag editor. Scroll to scale. Right-click resets.")
	@ConfigEditorButton(buttonText = "Edit")
	public Runnable editHud = () -> HudEditorScreen.open(DailyTasksHelper.INSTANCE.hud());

	@Expose
	public int hudX = 10;

	@Expose
	public int hudY = 10;

	@Expose
	public float hudScale = 1f;

	@Expose
	public List<DailyTask> completed = new ArrayList<>();

	@Expose
	public String completedDay = "";
}
