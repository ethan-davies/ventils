package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EarlyEnterZoneToggles {
	@Expose
	@ConfigOption(name = "At MID", desc = "Mid drop.")
	@ConfigEditorBoolean
	public boolean mid = true;

	@Expose
	@ConfigOption(name = "At SS", desc = "Simon Says.")
	@ConfigEditorBoolean
	public boolean ss = true;

	@Expose
	@ConfigOption(name = "At Low EE2", desc = "Low early enter 2.")
	@ConfigEditorBoolean
	public boolean lowEe2 = true;

	@Expose
	@ConfigOption(name = "At High EE2", desc = "High early enter 2.")
	@ConfigEditorBoolean
	public boolean highEe2 = true;

	@Expose
	@ConfigOption(name = "At Low EE3", desc = "Low early enter 3.")
	@ConfigEditorBoolean
	public boolean lowEe3 = true;

	@Expose
	@ConfigOption(name = "At High EE3", desc = "High early enter 3.")
	@ConfigEditorBoolean
	public boolean highEe3 = true;

	@Expose
	@ConfigOption(name = "In Core", desc = "Core.")
	@ConfigEditorBoolean
	public boolean core = true;

	@Expose
	@ConfigOption(name = "Outside Core", desc = "Outside core.")
	@ConfigEditorBoolean
	public boolean outsideCore = true;

	public boolean enabled(String id) {
		return switch (id) {
			case "mid" -> mid;
			case "ss" -> ss;
			case "low_ee2" -> lowEe2;
			case "high_ee2" -> highEe2;
			case "low_ee3" -> lowEe3;
			case "high_ee3" -> highEe3;
			case "core" -> core;
			case "outside_core" -> outsideCore;
			default -> false;
		};
	}
}
