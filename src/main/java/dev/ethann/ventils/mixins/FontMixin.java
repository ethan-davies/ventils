package dev.ethann.ventils.mixins;

import dev.ethann.ventils.features.cosmetics.Cosmetics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public class FontMixin {
	@ModifyVariable(
		method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",
		at = @At("HEAD"),
		argsOnly = true
	)
	private FormattedCharSequence ventils$replacePrepared(FormattedCharSequence text) {
		return Cosmetics.replace(text);
	}

	@ModifyVariable(method = "width(Lnet/minecraft/util/FormattedCharSequence;)I", at = @At("HEAD"), argsOnly = true)
	private FormattedCharSequence ventils$replaceWidthSequence(FormattedCharSequence text) {
		return Cosmetics.replace(text);
	}

	@ModifyVariable(method = "width(Lnet/minecraft/network/chat/FormattedText;)I", at = @At("HEAD"), argsOnly = true)
	private FormattedText ventils$replaceWidthText(FormattedText text) {
		return Cosmetics.replace(text);
	}

	@ModifyVariable(method = "width(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true)
	private String ventils$replaceWidthString(String text) {
		return Cosmetics.replace(text);
	}
}
