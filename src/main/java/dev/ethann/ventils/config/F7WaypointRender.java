package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class F7WaypointRender {
	@Expose
	@ConfigOption(name = "Render Text", desc = "Renders the name of the inactive waypoint.")
	@ConfigEditorBoolean
	public boolean text = true;

	@Expose
	@ConfigOption(name = "Render Box", desc = "Renders a box around the inactive waypoint.")
	@ConfigEditorBoolean
	public boolean box = true;

	@Expose
	@ConfigOption(name = "Fill boxes", desc = "When disabled, only the box outline is drawn.")
	@ConfigEditorBoolean
	public boolean fill = false;

	@Expose
	@ConfigOption(name = "Render Tracer", desc = "Draws a line from you to the closest required or stacked job.")
	@ConfigEditorBoolean
	public boolean tracer = true;

	@Expose
	@ConfigOption(name = "Show other terminals", desc = "Draws a gray box on inactive terminals that are not assigned to your class. No tracer.")
	@ConfigEditorBoolean
	public boolean otherInactive = false;

	@Expose
	@ConfigOption(name = "Hide Default", desc = "Hide the Hypixel names of inactive terminals.")
	@ConfigEditorBoolean
	public boolean hideDefault = true;

	@Expose
	@ConfigOption(name = "Depth check", desc = "Boxes show through walls when disabled.")
	@ConfigEditorBoolean
	public boolean depthCheck = false;
}
