package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LeapMessagesCategory {
	@Expose
	@ConfigOption(name = "Leap Messages", desc = "Sends a party chat line when you enter an F7/M7 early-enter zone.")
	@ConfigEditorBoolean
	public boolean enabled = false;

	@Expose
	@Accordion
	@ConfigOption(name = "Zones", desc = "Toggle which spots send a party chat line.")
	public EarlyEnterZoneToggles zones = new EarlyEnterZoneToggles();

	@Expose
	@Accordion
	@ConfigOption(name = "Boxes", desc = "World boxes for box-shaped leap zones.")
	public LeapMessageBoxes boxes = new LeapMessageBoxes();

	public boolean zoneEnabled(String id) {
		return zones.enabled(id);
	}

	public static class LeapMessageBoxes {
		@Expose
		@ConfigOption(name = "Show leap boxes", desc = "Draws a box around box-shaped leap zones. Hidden through walls.")
		@ConfigEditorBoolean
		public boolean enabled = true;

		@Expose
		@ConfigOption(name = "Fill boxes", desc = "When disabled, only the box outline is drawn.")
		@ConfigEditorBoolean
		public boolean fill = true;

		@Expose
		@ConfigOption(name = "Box color", desc = "Color of leap-zone and debug boxes.")
		@ConfigEditorColour
		public ChromaColour color = ChromaColour.fromStaticRGB(0, 255, 0, 255);
	}
}
