package io.github.xiewuzhiying.vs_addition.mixinducks.create.mechanical_arm;

import net.minecraft.core.BlockPos;

public interface ArmInteractionPointMixinDuck {
    void setPos(BlockPos pos);
    BlockPos getPos();
}
