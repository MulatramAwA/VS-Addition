package io.github.xiewuzhiying.vs_addition.mixinducks.minecraft;

import java.util.Optional;

public interface LevelChunkMixinDuck {
    Optional<Boolean> getIsInShipyard();
    void setIsInShipyard(boolean value);
}
