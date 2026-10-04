package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonClass;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DungeonsCategory {
	@Expose
	@ConfigOption(name = "Dungeon Class", desc = "Your selected dungeon class. Used by Goldor roles and later dungeon modules.")
	@ConfigEditorDropdown
	public DungeonClass dungeonClass = DungeonClass.ARCHER;

	@Expose
	@ConfigOption(name = "Auto Detect Class", desc = "Sets dungeon class from chat like \"You have selected the Healer Dungeon Class!\".")
	@ConfigEditorBoolean
	public boolean autoDetectClass = true;

	@Expose
	@Category(name = "F7/M7", desc = "Floor 7 and Master Mode floor 7 features.")
	public F7M7Category f7m7 = new F7M7Category();
}
