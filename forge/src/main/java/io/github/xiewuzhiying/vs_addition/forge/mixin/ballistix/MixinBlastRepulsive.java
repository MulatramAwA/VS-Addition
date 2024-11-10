package io.github.xiewuzhiying.vs_addition.forge.mixin.ballistix;

import ballistix.common.blast.Blast;
import ballistix.common.blast.BlastRepulsive;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Pseudo
@Mixin(BlastRepulsive.class)
public abstract class MixinBlastRepulsive extends Blast {
    protected MixinBlastRepulsive(Level world, BlockPos position) {
        super(world, position);
    }

    @ModifyVariable(
            method = "doExplode",
            at = @At("STORE"),
            index = 2,
            remap = false
    )
    private float modifyX(float x, @Share("isModified") LocalBooleanRef isModified, @Share("sharedY") LocalFloatRef sharedY, @Share("sharedZ") LocalFloatRef sharedZ) {
        if (VSGameUtilsKt.isChunkInShipyard(this.world, this.position.getX() >> 4, this.position.getZ() >> 4)) {
            final Vector3dc worldPos = VSGameUtilsKt.toWorldCoordinates(this.world, ConversionUtilsKt.getCenterJOMLD(this.position));
            isModified.set(true);
            sharedY.set((float) worldPos.y());
            sharedZ.set((float) worldPos.z());
            return (float) worldPos.x();
        } else {
            return x;
        }
    }

    @ModifyVariable(
            method = "doExplode",
            at = @At("STORE"),
            index = 3,
            remap = false
    )
    private float modifyY(float y, @Share("isModified") LocalBooleanRef isModified, @Share("sharedY") LocalFloatRef sharedY) {
        if (isModified.get()) {
            return sharedY.get();
        } else {
            return y;
        }
    }

    @ModifyVariable(
            method = "doExplode",
            at = @At("STORE"),
            index = 4,
            remap = false
    )
    private float modifyZ(float z, @Share("isModified") LocalBooleanRef isModified, @Share("sharedZ") LocalFloatRef sharedZ) {
        if (isModified.get()) {
            return sharedZ.get();
        } else {
            return z;
        }
    }
}
