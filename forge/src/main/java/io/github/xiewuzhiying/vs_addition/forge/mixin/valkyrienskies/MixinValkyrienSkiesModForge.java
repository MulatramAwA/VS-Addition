package io.github.xiewuzhiying.vs_addition.forge.mixin.valkyrienskies;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.xiewuzhiying.vs_addition.context.VSAdditionMassDatapackResolver;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.forge.common.ValkyrienSkiesModForge;

@Pseudo
@Mixin(ValkyrienSkiesModForge.class)
public abstract class MixinValkyrienSkiesModForge {
    @WrapOperation(
            method = "registerResourceManagers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/AddReloadListenerEvent;addListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
                    ordinal = 0
            ),
            remap = false
    )
    private void replace(AddReloadListenerEvent instance, PreparableReloadListener listener, Operation<Void> original) {
        original.call(instance, VSAdditionMassDatapackResolver.INSTANCE.getLoader());
    }
}
