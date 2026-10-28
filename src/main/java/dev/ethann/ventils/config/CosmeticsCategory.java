package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class CosmeticsCategory {
	@Expose
	@ConfigOption(name = "Show Custom Name", desc = "Replaces your username wherever it is shown on your screen.")
	@ConfigEditorBoolean
	public boolean showCustomName = true;

	@Expose
	@ConfigOption(name = "Name", desc = "Name shown in place of your username. Use & for colors, and &#RRGGBB for hex.")
	@ConfigEditorText
	public String name = "";

	@Expose
	@ConfigOption(name = "Show Name in Lore", desc = "Also replaces your name in item tooltips.")
	@ConfigEditorBoolean
	public boolean showInLore = true;

	@Expose
	@ConfigOption(name = "Hide Rank Prefix", desc = "Removes Hypixel rank prefixes such as [MVP+] from text. The player name keeps its color.")
	@ConfigEditorBoolean
	public boolean hideRankPrefix = false;
}
