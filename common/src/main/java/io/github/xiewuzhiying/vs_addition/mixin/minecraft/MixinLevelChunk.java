package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.LevelChunkMixinDuck;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk implements LevelChunkMixinDuck {

    @Unique
    private Boolean vs_addition$isInShipyard = null;

    @Override
    public Optional<Boolean> getIsInShipyard() {
        return this.vs_addition$isInShipyard == null ? Optional.empty() : Optional.of(this.vs_addition$isInShipyard);
    }

    @Override
    public void setIsInShipyard(boolean value) {
        this.vs_addition$isInShipyard = value;
    }
}
