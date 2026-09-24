package dev.ethann.ventils.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;

public class VentilsConfig extends Config {
	@Expose
	@Category(name = "Dungeons", desc = "Hypixel Skyblock dungeon features.")
	public DungeonsCategory dungeons = new DungeonsCategory();

	@Override
	public StructuredText getTitle() {
		return StructuredText.of("Ventils").aqua();
	}
}
