package dev.ethann.ventils.mixins;

import dev.ethann.ventils.features.cosmetics.Cosmetics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {
	@Inject(
		method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
		at = @At("HEAD")
	)
	private void ventils$tooltipStart(
		Font font,
		List<ClientTooltipComponent> lines,
		int xo,
		int yo,
		ClientTooltipPositioner positioner,
		Identifier style,
		CallbackInfo ci
	) {
		Cosmetics.beginTooltip();
	}

	@Inject(
		method = "tooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;)V",
		at = @At("RETURN")
	)
	private void ventils$tooltipEnd(
		Font font,
		List<ClientTooltipComponent> lines,
		int xo,
		int yo,
		ClientTooltipPositioner positioner,
		Identifier style,
		CallbackInfo ci
	) {
		Cosmetics.endTooltip();
	}
}
