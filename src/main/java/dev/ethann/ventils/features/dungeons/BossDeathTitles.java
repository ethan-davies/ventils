package dev.ethann.ventils.features.dungeons;

import dev.ethann.ventils.Ventils;
import dev.ethann.ventils.config.BossDeathTitlesCategory;
import dev.ethann.ventils.features.ChatFeature;
import dev.ethann.ventils.utils.Titles;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.function.Predicate;

public final class BossDeathTitles extends ChatFeature {
	public static final BossDeathTitles INSTANCE = new BossDeathTitles();

	private static final List<TitleTrigger> TRIGGERS = List.of(
		new TitleTrigger(
			events -> events.maxorDead,
			"Maxor Dead!",
			ChatFormatting.LIGHT_PURPLE,
			exact("[BOSS] Maxor: I'M TOO YOUNG TO DIE AGAIN!")
		),
		new TitleTrigger(
			events -> events.stormDead,
			"Storm Dead!",
			ChatFormatting.AQUA,
			exact("[BOSS] Storm: I should have known that I stood no chance.")
		),
		new TitleTrigger(
			events -> events.goldorDead,
			"Goldor Dead!",
			ChatFormatting.GRAY,
			exact("[BOSS] Goldor: ...")
		),
		new TitleTrigger(
			events -> events.necronDead,
			"Necron Dead!",
			ChatFormatting.RED,
			exact("[BOSS] Necron: All this, for nothing...")
		),
		new TitleTrigger(
			events -> events.maxorStunned,
			"Maxor Stunned!",
			ChatFormatting.LIGHT_PURPLE,
			exact(
				"[BOSS] Maxor: YOU TRICKED ME!",
				"[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!"
			)
		),
		new TitleTrigger(
			events -> events.stormCrushed,
			"Storm Crushed!",
			ChatFormatting.AQUA,
			exact(
				"[BOSS] Storm: Oof",
				"[BOSS] Storm: Ouch, that hurt!"
			)
		),
		new TitleTrigger(
			events -> events.allPlayersInCore,
			"All Players in Core!",
			ChatFormatting.GRAY,
			exact(
				"[BOSS] Goldor: You have done it, you destroyed the factory…",
				"[BOSS] Goldor: You have done it, you destroyed the factory..."
			)
		)
	);

	private BossDeathTitles() {
	}

	@Override
	public String name() {
		return "Boss Death Titles";
	}

	@Override
	public boolean isEnabled() {
		return config().enabled;
	}

	@Override
	public void onGameChat(String stripped) {
		if (!isEnabled()) {
			return;
		}
		BossDeathTitlesCategory.Events events = config().events;
		for (TitleTrigger trigger : TRIGGERS) {
			if (trigger.enabled.test(events) && trigger.match.test(stripped)) {
				Titles.show(trigger.title, trigger.color);
				return;
			}
		}
	}

	private static BossDeathTitlesCategory config() {
		return Ventils.CONFIG.getInstance().dungeons.bossDeathTitles;
	}

	private static Predicate<String> exact(String... messages) {
		return stripped -> {
			for (String message : messages) {
				if (stripped.equals(message)) {
					return true;
				}
			}
			return false;
		};
	}

	private record TitleTrigger(
		Predicate<BossDeathTitlesCategory.Events> enabled,
		String title,
		ChatFormatting color,
		Predicate<String> match
	) {
	}
}
