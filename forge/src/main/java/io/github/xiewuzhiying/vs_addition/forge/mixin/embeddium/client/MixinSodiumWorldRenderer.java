package io.github.xiewuzhiying.vs_addition.forge.mixin.embeddium.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(SodiumWorldRenderer.class)
@Restriction(
        require = @Condition(value = "embeddium", versionPredicates = ">=0.3.12+mc1.20.1")
)
public abstract class MixinSodiumWorldRenderer {
    @ModifyExpressionValue(
            method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;ENABLE_BLOCKENTITY_CULLING:Z"
            ),
            remap = false
    )
    private boolean enableBlockEntityCulling1(boolean original) {
        if (VSAdditionConfig.CLIENT.getEmbeddium().getEnableBlockEntityCullOnShips()) {
            return true;
        } else {
            return original;
        }
    }

    @ModifyExpressionValue(
            method = "renderGlobalBlockEntities",
            at = @At(
                    value = "FIELD",
                    target = "Lme/jellysquid/mods/sodium/client/render/SodiumWorldRenderer;ENABLE_BLOCKENTITY_CULLING:Z"
            ),
            remap = false
    )
    private boolean enableBlockEntityCulling2(boolean original) {
        if (VSAdditionConfig.CLIENT.getEmbeddium().getEnableBlockEntityCullOnShips()) {
            return true;
        } else {
            return original;
        }
    }

    @WrapOperation(
            method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/viewport/Viewport;isBoxVisible(Lnet/minecraft/world/phys/AABB;)Z"
            ),
            remap = false
    )
    private boolean transformToWorld(Viewport instance, AABB aabb, Operation<Boolean> original) {
        if (VSAdditionConfig.CLIENT.getEmbeddium().getEnableBlockEntityCullOnShips() && VSGameUtilsKt.isBlockInShipyard(Minecraft.getInstance().level, aabb.minX, aabb.minY, aabb.minZ)) {
            return original.call(instance, VSGameUtilsKt.transformAabbToWorld(Minecraft.getInstance().level, aabb));
        } else {
            return original.call(instance, aabb);
        }
    }
}
