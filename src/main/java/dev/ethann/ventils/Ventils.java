package dev.ethann.ventils;

import dev.ethann.ventils.command.VentilsCommands;
import dev.ethann.ventils.config.VentilsConfig;
import dev.ethann.ventils.features.FeatureRegistry;
import dev.ethann.ventils.features.dungeons.BossDeathTitles;
import dev.ethann.ventils.features.dungeons.DungeonClassFeature;
import dev.ethann.ventils.features.dungeons.EarlyEnterHelper;
import dev.ethann.ventils.features.dungeons.InactiveWaypoints;
import dev.ethann.ventils.features.dungeons.LeapMessages;
import dev.ethann.ventils.features.dungeons.PlatformHighlight;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ventils implements ClientModInitializer {
	public static final String MOD_ID = "ventils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ManagedConfig<VentilsConfig> CONFIG;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitializeClient() {
		CONFIG = ManagedConfig.create(
			FabricLoader.getInstance().getConfigDir().resolve("ventils.json").toFile(),
			VentilsConfig.class
		);
		VentilsCommands.register();
		FeatureRegistry.register(
			DungeonClassFeature.INSTANCE,
			InactiveWaypoints.INSTANCE,
			EarlyEnterHelper.INSTANCE,
			LeapMessages.INSTANCE,
			BossDeathTitles.INSTANCE,
			PlatformHighlight.INSTANCE
		);
		LOGGER.info("Ventils loaded");
	}
}
