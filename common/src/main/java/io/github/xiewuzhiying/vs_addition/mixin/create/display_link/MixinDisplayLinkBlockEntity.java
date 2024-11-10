package io.github.xiewuzhiying.vs_addition.mixin.create.display_link;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(DisplayLinkBlockEntity.class)
public abstract class MixinDisplayLinkBlockEntity extends SmartBlockEntity {
    public MixinDisplayLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @ModifyExpressionValue(
            method = "updateGatheredData",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;isLoaded(Lnet/minecraft/core/BlockPos;)Z",
                    ordinal = 1
            )
    )
    public boolean distanceLimit(boolean original, @Local(ordinal = 0) BlockPos sourcePosition, @Local(ordinal = 1) BlockPos targetPosition) {
        return original || ConversionUtilsKt.squaredDistanceBetweenInclShips(this.level, sourcePosition, targetPosition) > Mth.square(AllConfigs.server().logistics.displayLinkRange.get());
    }
}
