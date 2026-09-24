package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class F7WaypointKinds {
	@Expose
	@ConfigOption(name = "Show Terminals", desc = "Shows inactive terminals.")
	@ConfigEditorBoolean
	public boolean terminals = true;

	@Expose
	@ConfigOption(name = "Show Devices", desc = "Shows inactive devices.")
	@ConfigEditorBoolean
	public boolean devices = true;

	@Expose
	@ConfigOption(name = "Show Levers", desc = "Shows inactive levers.")
	@ConfigEditorBoolean
	public boolean levers = true;
}
