package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.LevelChunkMixinDuck;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk implements LevelChunkMixinDuck {

    @Unique
    private boolean vs_addition$isInShipyard = false;

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;)V",
            at = @At("TAIL")
    )
    private void checkIfInShipyard(Level level, ChunkPos pos, CallbackInfo ci) {
        this.vs_addition$isInShipyard = VSGameUtilsKt.isChunkInShipyard(level, pos.x, pos.z);
    }

    @Override
    public boolean isInShipyard() {
        return this.vs_addition$isInShipyard;
    }
}
