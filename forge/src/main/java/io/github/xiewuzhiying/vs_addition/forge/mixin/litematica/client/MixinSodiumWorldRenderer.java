package io.github.xiewuzhiying.vs_addition.forge.mixin.litematica.client;

import com.bawnorton.mixinsquared.TargetHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.litematica.world.WorldSchematic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

@Pseudo
@Restriction(
        require = @Condition("forgematica")
)
@Mixin(value = SodiumWorldRenderer.class, priority = 1200)
public abstract class MixinSodiumWorldRenderer {
    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.mod_compat.sodium.MixinSodiumWorldRenderer",
            name = "renderShipBlockEntityInShipyard"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void cancelIfWorldSchematic(final PoseStack instance, final double d, final double e,
                                        final double f, final PoseStack ignore, final RenderBuffers bufferBuilders,
                                        final Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions,
                                        final float tickDelta, final MultiBufferSource.BufferSource immediate, final double camX, final double camY,
                                        final double camZ, final BlockEntityRenderDispatcher dispatcher, final BlockEntity entity, final CallbackInfo ci) {
        if (dispatcher.level instanceof WorldSchematic) {
            ci.cancel();
        }
    }
}
