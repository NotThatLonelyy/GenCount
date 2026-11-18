package com.notthatlonely.gencount.client.mixin;

import com.notthatlonely.gencount.client.GenCountKeybinding;
import java.util.Arrays;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the GenCount key to the vanilla controls screen without needing Fabric API. */
@Mixin(Options.class)
public class OptionsMixin {

    @Mutable @Shadow @Final public KeyMapping[] keyMappings;

    @Inject(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V", at = @At("TAIL"))
    private void genCount$registerKeyMappings(final CallbackInfo info) {
        final KeyMapping mapping = GenCountKeybinding.TOGGLE;
        final KeyMapping[] original = this.keyMappings;

        for (final KeyMapping existing : original) {
            if (existing == mapping) {
                return;
            }
        }

        final KeyMapping[] extended = Arrays.copyOf(original, original.length + 1);
        extended[original.length] = mapping;
        this.keyMappings = extended;
    }
}
