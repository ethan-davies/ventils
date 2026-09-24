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
	@Category(name = "F7/M7 Roles", desc = "Terminal, device and lever helpers for Goldor.")
	public F7RolesCategory f7Roles = new F7RolesCategory();

	@Expose
	@Category(name = "Boss Death Titles", desc = "F7/M7 titles for wither deaths and related events.")
	public BossDeathTitlesCategory bossDeathTitles = new BossDeathTitlesCategory();

	@Expose
	@Category(name = "Platform Highlight", desc = "Highlights the F7/M7 Necron platform.")
	public PlatformHighlightCategory platformHighlight = new PlatformHighlightCategory();

	@Expose
	@Category(name = "Early Enter Helper", desc = "Shows the first player in F7/M7 early-enter zones.")
	public EarlyEnterCategory earlyEnter = new EarlyEnterCategory();

	@Expose
	@Category(name = "Leap Messages", desc = "Party chat and boxes for F7/M7 early-enter zones.")
	public LeapMessagesCategory leapMessages = new LeapMessagesCategory();
}
