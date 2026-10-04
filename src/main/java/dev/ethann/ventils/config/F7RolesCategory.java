package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import dev.ethann.ventils.features.HudEditorScreen;
import dev.ethann.ventils.features.dungeons.InactiveWaypoints;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class F7RolesCategory {
	@Expose
	@ConfigOption(name = "Inactive Waypoints", desc = "Shows inactive terminals, devices and levers in F7/M7 Goldor.")
	@ConfigEditorBoolean
	public boolean inactiveWaypoints = true;

	@Expose
	@Accordion
	@ConfigOption(name = "Objects", desc = "Which inactive objects to show.")
	public F7WaypointKinds kinds = new F7WaypointKinds();

	@Expose
	@Accordion
	@ConfigOption(name = "Render", desc = "How waypoints are drawn.")
	public F7WaypointRender render = new F7WaypointRender();

	@Expose
	@Accordion
	@ConfigOption(name = "Colors", desc = "Waypoint colors by role.")
	public F7WaypointColors colors = new F7WaypointColors();

	@Expose
	@ConfigOption(name = "Term Info HUD", desc = "Shows your class terminals, devices and levers for the current Goldor phase.")
	@ConfigEditorBoolean
	public boolean termInfoHud = true;

	@ConfigOption(name = "Edit Term Info HUD", desc = "Opens a drag editor. Scroll to scale. Right-click resets.")
	@ConfigEditorButton(buttonText = "Edit")
	public Runnable editTermInfoHud = () -> HudEditorScreen.open(InactiveWaypoints.INSTANCE.hud());

	@Expose
	public int termInfoX = 10;

	@Expose
	public int termInfoY = 10;

	@Expose
	public float termInfoScale = 1f;
}
