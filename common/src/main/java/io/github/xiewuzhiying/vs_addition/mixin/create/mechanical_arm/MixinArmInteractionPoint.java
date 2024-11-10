package io.github.xiewuzhiying.vs_addition.mixin.create.mechanical_arm;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.mechanical_arm.ArmInteractionPointMixinDuck;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(ArmInteractionPoint.class)
public abstract class MixinArmInteractionPoint implements ArmInteractionPointMixinDuck {
    @Shadow @Final @Mutable
    protected BlockPos pos;

    @Override
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }
}
