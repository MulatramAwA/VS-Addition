package io.github.xiewuzhiying.vs_addition.fabric.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.util.block.BlockRayTrace;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import static io.github.xiewuzhiying.vs_addition.util.ShipUtilsKt.clipIncludeShipsWrapper;

@Pseudo
@Mixin(BlockRayTrace.class)
public abstract class MixinBlockRayTrace {
    @WrapMethod(
            method = "rayTraceBlocks",
            remap = false
    )
    private static BlockHitResult wrap(Level level, ClipContext context, Operation<BlockHitResult> original) {
        return (BlockHitResult) clipIncludeShipsWrapper(level, context, original::call);
    }
}
