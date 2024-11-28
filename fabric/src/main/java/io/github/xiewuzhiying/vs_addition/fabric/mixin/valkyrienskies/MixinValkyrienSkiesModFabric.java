package io.github.xiewuzhiying.vs_addition.fabric.mixin.valkyrienskies;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.xiewuzhiying.vs_addition.context.VSAdditionMassDatapackResolver;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.config.VSEntityHandlerDataLoader;
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Pseudo
@Mixin(ValkyrienSkiesModFabric.class)
public abstract class MixinValkyrienSkiesModFabric {
    @WrapOperation(
            method = "onInitialize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/fabricmc/fabric/api/resource/ResourceManagerHelper;registerReloadListener(Lnet/fabricmc/fabric/api/resource/IdentifiableResourceReloadListener;)V"
            ),
            remap = false
    )
    private void replace(ResourceManagerHelper instance, IdentifiableResourceReloadListener identifiableResourceReloadListener, Operation<Void> original, @Local VSEntityHandlerDataLoader loader2) {
        original.call(instance, new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(ValkyrienSkiesMod.MOD_ID, "vs_mass");
            }

            @Override
            public CompletableFuture<Void> reload(@NotNull PreparationBarrier stage, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller preparationsProfiler, @NotNull ProfilerFiller reloadProfiler, @NotNull Executor backgroundExecutor, @NotNull Executor gameExecutor) {
                return VSAdditionMassDatapackResolver.INSTANCE.getLoader().reload(
                        stage, resourceManager, preparationsProfiler, reloadProfiler,
                        backgroundExecutor, gameExecutor
                ).thenAcceptBoth(
                        loader2.reload(
                                stage, resourceManager, preparationsProfiler, reloadProfiler,
                                backgroundExecutor, gameExecutor
                        ),
                        (a, b) -> {}
                );
            }
        });
    }
}
