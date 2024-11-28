package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Pseudo
@Mixin(CompassItem.class)
public abstract class MixinCompassItem {
    @WrapOperation(
            method = "inventoryTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;existsAtPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean checkDoesShipExist(PoiManager instance, ResourceKey<PoiType> type, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true) Level level) {
        if (VSGameUtilsKt.isBlockInShipyard(level, pos)) {
            return original.call(instance, type, pos) && VSGameUtilsKt.getShipManagingPos(level, pos) != null;
        } else {
            return original.call(instance, type, pos);
        }
    }
}
