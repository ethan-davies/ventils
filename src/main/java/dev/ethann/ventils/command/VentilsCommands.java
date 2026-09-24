package dev.ethann.ventils.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.features.HudEditorScreen;
import dev.ethann.ventils.features.dungeons.InactiveWaypoints;
import dev.ethann.ventils.render.RenderDebug;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public final class VentilsCommands {
	private VentilsCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			LiteralCommandNode<FabricClientCommandSource> ventils = dispatcher.register(
				ClientCommands.literal("ventils")
					.executes(context -> {
						Minecraft.getInstance().schedule(Ventils.CONFIG::openConfigGui);
						return 0;
					})
					.then(ClientCommands.literal("hud").executes(context -> {
						HudEditorScreen.open(null, InactiveWaypoints.INSTANCE.hud());
						return 0;
					}))
					.then(ClientCommands.literal("debug")
						.executes(context -> RenderDebug.toggleAtPlayer())
						.then(ClientCommands.literal("title").executes(context -> RenderDebug.showTitle()))
						.then(ClientCommands.literal("box").executes(context -> RenderDebug.toggleBox()))
					)
			);
			dispatcher.register(ClientCommands.literal("vt")
				.executes(ventils.getCommand())
				.redirect(ventils));
		});
	}
}
