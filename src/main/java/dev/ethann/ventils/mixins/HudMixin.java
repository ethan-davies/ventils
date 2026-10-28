package dev.ethann.ventils.mixins;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
	private static final float TITLE_SCALE = 2.5f / 4f;
	private static final float SUBTITLE_SCALE = 1.5f / 2f;

	@Inject(
		method = "extractTitle",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
			ordinal = 0,
			shift = At.Shift.AFTER,
			remap = false
		)
	)
	private void ventils$smallerTitle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		graphics.pose().scale(TITLE_SCALE, TITLE_SCALE);
	}

	@Inject(
		method = "extractTitle",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
			ordinal = 1,
			shift = At.Shift.AFTER,
			remap = false
		)
	)
	private void ventils$smallerSubtitle(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		graphics.pose().scale(SUBTITLE_SCALE, SUBTITLE_SCALE);
	}
}
