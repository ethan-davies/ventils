package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BossDeathTitlesCategory {
	@Expose
	@ConfigOption(name = "Boss Death Titles", desc = "Shows a title on F7/M7 wither deaths and related events.")
	@ConfigEditorBoolean
	public boolean enabled = true;

	@Expose
	@Accordion
	@ConfigOption(name = "Events", desc = "Toggle each title independently.")
	public Events events = new Events();

	public static class Events {
		@Expose
		@ConfigOption(name = "Maxor Dead", desc = "When Maxor dies.")
		@ConfigEditorBoolean
		public boolean maxorDead = true;

		@Expose
		@ConfigOption(name = "Storm Dead", desc = "When Storm dies.")
		@ConfigEditorBoolean
		public boolean stormDead = true;

		@Expose
		@ConfigOption(name = "Goldor Dead", desc = "When Goldor dies.")
		@ConfigEditorBoolean
		public boolean goldorDead = true;

		@Expose
		@ConfigOption(name = "Necron Dead", desc = "When Necron dies.")
		@ConfigEditorBoolean
		public boolean necronDead = true;

		@Expose
		@ConfigOption(name = "Maxor Stunned", desc = "When Maxor is stunned by the laser.")
		@ConfigEditorBoolean
		public boolean maxorStunned = true;

		@Expose
		@ConfigOption(name = "Storm Crushed", desc = "When Storm is crushed in the crush.")
		@ConfigEditorBoolean
		public boolean stormCrushed = true;

		@Expose
		@ConfigOption(name = "All Players in Core", desc = "When Goldor says the factory is destroyed.")
		@ConfigEditorBoolean
		public boolean allPlayersInCore = true;
	}
}
