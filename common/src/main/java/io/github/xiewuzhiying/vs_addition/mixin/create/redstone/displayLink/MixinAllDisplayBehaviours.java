package io.github.xiewuzhiying.vs_addition.mixin.create.redstone.displayLink;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.content.redstone.displayLink.DisplayBehaviour;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.displayLink.ShipDataDisplaySource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(AllDisplayBehaviours.class)
public abstract class MixinAllDisplayBehaviours {
    @Shadow(remap = false) @Final public static Map<ResourceLocation, DisplayBehaviour> GATHERER_BEHAVIOURS;

    @WrapMethod(
            method = "sourcesOf(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Ljava/util/List;"
    )
    private static List<DisplaySource> checkIfShip(LevelAccessor level, BlockPos pos, Operation<List<DisplaySource>> original) {
        final List<DisplaySource> originalLIst = original.call(level, pos);
        if (!originalLIst.isEmpty()) return originalLIst;

        if (level instanceof ServerLevel serverLevel) {
            return vs_addition$returnIfHasShip(serverLevel, pos);
        } else if (level instanceof ClientLevel clientLevel) {
            return vs_addition$returnIfHasShip(clientLevel, pos);
        } else {
            return originalLIst;
        }
    }

    @Unique
    private static List<DisplaySource> vs_addition$returnIfHasShip(Level level, BlockPos pos) {
        if (VSGameUtilsKt.getShipManagingPos(level, pos) != null && GATHERER_BEHAVIOURS.get(ShipDataDisplaySource.Companion.getId()) instanceof DisplaySource source) {
            return Collections.singletonList(source);
        } else {
            return Collections.emptyList();
        }
    }
}
