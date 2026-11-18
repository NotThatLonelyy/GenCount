package com.notthatlonely.gencount.client.mixin;

import com.notthatlonely.gencount.client.GenCountModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class OverlayRenderStateMixin {

    @Inject(
            method =
                    "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"))
    private void genCount$renderLabels(
            final GuiGraphicsExtractor graphics,
            final DeltaTracker deltaTracker,
            final CallbackInfo info) {
        GenCountModule.render(graphics, deltaTracker);
    }
}
