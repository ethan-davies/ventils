package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import dev.ethann.ventils.features.HudEditorScreen;
import dev.ethann.ventils.features.dungeons.EarlyEnterHelper;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class EarlyEnterCategory {
	@Expose
	@ConfigOption(name = "Early Enter Helper", desc = "Shows the first player who entered each F7/M7 early-enter zone.")
	@ConfigEditorBoolean
	public boolean enabled = true;

	@Expose
	@ConfigOption(name = "Show leap reminder", desc = "Shows a title to leap to the early-enter player when a Goldor section starts.")
	@ConfigEditorBoolean
	public boolean showLeapReminder = true;

	@Expose
	@ConfigOption(name = "Leap reminder text", desc = "Title text. Use {class} or {player}, and & for colors.")
	@ConfigEditorText
	public String leapReminderText = "&dLeap to {class}";

	@ConfigOption(name = "Edit Player Positions HUD", desc = "Opens a drag editor. Scroll to scale. Right-click resets.")
	@ConfigEditorButton(buttonText = "Edit")
	public Runnable editHud = () -> HudEditorScreen.open(EarlyEnterHelper.INSTANCE.hud());

	@Expose
	@Accordion
	@ConfigOption(name = "Zones", desc = "Toggle which early-enter spots are tracked.")
	public EarlyEnterZoneToggles zones = new EarlyEnterZoneToggles();

	@Expose
	public int hudX = 10;

	@Expose
	public int hudY = 60;

	@Expose
	public float hudScale = 1f;

	public boolean zoneEnabled(String id) {
		return zones.enabled(id);
	}
}
