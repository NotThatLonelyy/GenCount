package com.notthatlonely.gencount.client.mixin;

import com.notthatlonely.gencount.client.GenCountKeybinding;
import com.notthatlonely.gencount.client.GenCountModule;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void genCount$onClientTick(final CallbackInfo info) {
        while (GenCountKeybinding.TOGGLE.consumeClick()) {
            GenCountModule.toggle();
        }

        GenCountModule.onClientTick(Minecraft.getInstance());
    }
}
