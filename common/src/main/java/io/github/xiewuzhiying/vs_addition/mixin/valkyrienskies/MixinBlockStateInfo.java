package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.xiewuzhiying.vs_addition.context.VSAdditionMassDatapackResolver;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.BlockStateInfo;
import org.valkyrienskies.mod.common.BlockStateInfoProvider;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

@Pseudo
@Mixin(BlockStateInfo.class)
public abstract class MixinBlockStateInfo {
    @Final @Shadow private static MappedRegistry<BlockStateInfoProvider> REGISTRY;

    @WrapWithCondition(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    private <V, T extends V> boolean cancel(Registry<V> registry, ResourceLocation name, T value) {
        return false;
    }

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    private void inject(CallbackInfo ci) {
        Registry.register(REGISTRY, new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "data"), VSAdditionMassDatapackResolver.INSTANCE);
    }
}
