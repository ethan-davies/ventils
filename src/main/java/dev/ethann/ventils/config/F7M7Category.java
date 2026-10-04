package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class F7M7Category {
	@Expose
	@Accordion
	@ConfigOption(name = "F7/M7 Roles", desc = "Terminal, device and lever helpers for Goldor.")
	public F7RolesCategory f7Roles = new F7RolesCategory();

	@Expose
	@Accordion
	@ConfigOption(name = "Boss Death Titles", desc = "F7/M7 titles for wither deaths and related events.")
	public BossDeathTitlesCategory bossDeathTitles = new BossDeathTitlesCategory();

	@Expose
	@Accordion
	@ConfigOption(name = "Platform Highlight", desc = "Highlights the F7/M7 Necron platform.")
	public PlatformHighlightCategory platformHighlight = new PlatformHighlightCategory();

	@Expose
	@Accordion
	@ConfigOption(name = "Early Enter Helper", desc = "Shows the first player in F7/M7 early-enter zones.")
	public EarlyEnterCategory earlyEnter = new EarlyEnterCategory();

	@Expose
	@Accordion
	@ConfigOption(name = "Leap Messages", desc = "Party chat and boxes for F7/M7 early-enter zones.")
	public LeapMessagesCategory leapMessages = new LeapMessagesCategory();
}
