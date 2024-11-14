package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.LevelChunkMixinDuck;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    @WrapOperation(
            method = "sendParticles(Lnet/minecraft/server/level/ServerPlayer;ZDDDLnet/minecraft/network/protocol/Packet;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;closerToCenterThan(Lnet/minecraft/core/Position;D)Z"
            )
    )
    private boolean inclShips(final BlockPos instance, final Position position, final double v, final Operation<Boolean> original) {
        return ConversionUtilsKt.squaredDistanceBetweenInclShips((ServerLevel)(Object)this, instance, position) < Mth.square(v);
    }

    @Inject(
            method = "tickChunk",
            at = @At("HEAD")
    )
    private void tickChunk(final LevelChunk chunk, final int randomTickSpeed, CallbackInfo ci, final @Share("isChunkInShipyard") LocalBooleanRef isChunkInShipyard) {
        final Optional<Boolean> isInShipyard = ((LevelChunkMixinDuck)chunk).getIsInShipyard();
        if (isInShipyard.isEmpty()) {
            final boolean bl = VSGameUtilsKt.isChunkInShipyard((ServerLevel)(Object)this, chunk.getPos().x, chunk.getPos().z);
            ((LevelChunkMixinDuck)chunk).setIsInShipyard(bl);
            isChunkInShipyard.set(bl);
        } else {
            isChunkInShipyard.set(isInShipyard.get());
        }
    }

    @WrapOperation(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"
            )
    )
    private Holder<Biome> getBiomeInWorldSpace(final ServerLevel instance, final BlockPos blockPos, final Operation<Holder<Biome>> original, final @Share("isChunkInShipyard") LocalBooleanRef isChunkInShipyard) {
        if (isChunkInShipyard.get()) {
            final ServerShip ship = VSGameUtilsKt.getShipManagingPos(instance, blockPos);
            if (ship != null) {
                final Vector3d pos = ConversionUtilsKt.getCenterJOMLD(blockPos);
                ship.getTransform().getShipToWorld().transformPosition(pos);
                return original.call(instance, ConversionUtilsKt.getToBlockPos(pos));
            }
        }
        return original.call(instance, blockPos);
    }
}
