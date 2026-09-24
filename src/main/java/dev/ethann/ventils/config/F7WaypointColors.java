package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class F7WaypointColors {
	@Expose
	@ConfigOption(name = "Required Color", desc = "Color for waypoints assigned as required for your class.")
	@ConfigEditorColour
	public ChromaColour required = ChromaColour.fromStaticRGB(0, 255, 0, 255);

	@Expose
	@ConfigOption(name = "Stacked Color", desc = "Color for waypoints where you should stack.")
	@ConfigEditorColour
	public ChromaColour stacked = ChromaColour.fromStaticRGB(255, 255, 0, 255);

	@Expose
	@ConfigOption(name = "Optional Color", desc = "Color for waypoints assigned as optional for your class.")
	@ConfigEditorColour
	public ChromaColour optional = ChromaColour.fromStaticRGB(0, 127, 255, 255);
}
