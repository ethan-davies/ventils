package dev.ethann.ventils.features;

import dev.ethann.ventils.render.RenderDebug;
import dev.ethann.ventils.render.WorldRenderQueue;
import dev.ethann.ventils.utils.TextUtils;
import dev.ethann.ventils.utils.skyblock.dungeon.DungeonUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

import java.util.ArrayList;
import java.util.List;

public final class FeatureRegistry {
	private static final List<Feature> FEATURES = new ArrayList<>();
	private static final List<TickListener> TICK = new ArrayList<>();
	private static final List<ChatListener> CHAT = new ArrayList<>();
	private static final List<SessionListener> SESSION = new ArrayList<>();
	private static final List<WorldRenderListener> WORLD = new ArrayList<>();
	private static final List<HudFeature> HUDS = new ArrayList<>();

	private FeatureRegistry() {
	}

	public static List<Feature> features() {
		return List.copyOf(FEATURES);
	}

	public static void register(Feature... features) {
		for (Feature feature : features) {
			FEATURES.add(feature);
			if (feature instanceof TickListener tick) {
				TICK.add(tick);
			}
			if (feature instanceof ChatListener chat) {
				CHAT.add(chat);
			}
			if (feature instanceof SessionListener session) {
				SESSION.add(session);
			}
			if (feature instanceof WorldRenderListener world) {
				WORLD.add(world);
			}
			if (feature instanceof HudFeature hud) {
				HUDS.add(hud);
			}
		}
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			DungeonUtils.tick();
			for (TickListener listener : TICK) {
				listener.onTick(client);
			}
		});
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) {
				return;
			}
			String stripped = TextUtils.strip(message.getString());
			for (ChatListener listener : CHAT) {
				listener.onGameChat(stripped);
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			DungeonUtils.reset();
			for (SessionListener listener : SESSION) {
				listener.onJoin();
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			DungeonUtils.reset();
			for (SessionListener listener : SESSION) {
				listener.onDisconnect();
			}
		});
		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			for (WorldRenderListener listener : WORLD) {
				listener.onWorldRender();
			}
			RenderDebug.extract();
			WorldRenderQueue.flush(context);
		});
		for (HudFeature feature : HUDS) {
			Hud hud = feature.hud();
			HudElementRegistry.addLast(hud.id(), hud::renderLive);
		}
	}
}
