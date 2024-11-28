package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.xiewuzhiying.vs_addition.context.VSAdditionMassDatapackResolver;
import kotlin.Triple;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.config.MassDatapackResolver;
import org.valkyrienskies.physics_api.voxel.Lod1LiquidBlockState;
import org.valkyrienskies.physics_api.voxel.Lod1SolidBlockState;

import java.util.List;

@Pseudo
@Mixin(value = MinecraftServer.class, priority = 1500)
public abstract class MixinMinecraftServer {
    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.MixinMinecraftServer",
            name = "postCreateLevels"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/mod/common/config/MassDatapackResolver;getRegisteredBlocks()Z",
                    remap = false
            )
    )
    private boolean modify0(boolean original) {
        return VSAdditionMassDatapackResolver.INSTANCE.getRegisteredBlocks();
    }

    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.MixinMinecraftServer",
            name = "postCreateLevels"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/mod/common/config/MassDatapackResolver;registerAllBlockStates(Ljava/lang/Iterable;)V",
                    remap = false
            )
    )
    private void modify1(MassDatapackResolver instance, Iterable<BlockState> blockStates, Operation<Void> original) {
        VSAdditionMassDatapackResolver.INSTANCE.registerAllBlockStates(blockStates);
    }

    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.MixinMinecraftServer",
            name = "postCreateLevels"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/mod/common/config/MassDatapackResolver;getSolidBlockStates()Ljava/util/List;",
                    remap = false
            )
    )
    private List<Lod1SolidBlockState> modify2(List<Lod1SolidBlockState> original) {
        return VSAdditionMassDatapackResolver.INSTANCE.getSolidBlockStates();
    }

    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.MixinMinecraftServer",
            name = "postCreateLevels"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/mod/common/config/MassDatapackResolver;getLiquidBlockStates()Ljava/util/List;",
                    remap = false
            )
    )
    private List<Lod1LiquidBlockState> modify3(List<Lod1LiquidBlockState> original) {
        return VSAdditionMassDatapackResolver.INSTANCE.getLiquidBlockStates();
    }

    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.MixinMinecraftServer",
            name = "postCreateLevels"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/mod/common/config/MassDatapackResolver;getBlockStateData()Ljava/util/List;",
                    remap = false
            )
    )
    private List<Triple<Integer, Integer, Integer>> modify4(List<Triple<Integer, Integer, Integer>> original) {
        return VSAdditionMassDatapackResolver.INSTANCE.getBlockStateData();
    }
}
