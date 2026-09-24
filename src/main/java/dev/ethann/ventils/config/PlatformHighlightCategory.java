package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.ChromaColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PlatformHighlightCategory {
	@Expose
	@ConfigOption(name = "Platform Highlight", desc = "Draws a 3x3 box on the F7/M7 Necron platform.")
	@ConfigEditorBoolean
	public boolean enabled = true;

	@Expose
	@ConfigOption(name = "Fill box", desc = "When disabled, only the box outline is drawn.")
	@ConfigEditorBoolean
	public boolean fill = false;

	@Expose
	@ConfigOption(name = "Box color", desc = "Color of the platform box.")
	@ConfigEditorColour
	public ChromaColour color = ChromaColour.fromStaticRGB(0, 255, 0, 255);
}
