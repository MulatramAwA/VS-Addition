package io.github.xiewuzhiying.vs_addition.forge.mixin.cbcmodernwarfare;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

@Pseudo
@Mixin(targets = "riftyboi.cbcmodernwarfare.cannon_control.compact_mount.CompactCannonMountBlockEntity")
public interface CompactCannonMountBlockEntityAccessor {

    @Invoker(value = "assemble", remap = false)
    void vs_addition$assemble();

    @Invoker(value = "disassemble", remap = false)
    void vs_addition$disassemble();

    @Accessor(value = "cannonYaw", remap = false)
    float vs_addition$getCannonYaw();

    @Accessor(value = "cannonPitch", remap = false)
    float  vs_addition$getCannonPitch();

    @Invoker(value = "isRunning", remap = false)
    boolean vs_addition$isRunning();

    @Invoker(value = "getContraption", remap = false)
    PitchOrientedContraptionEntity vs_addition$getContraption();

    @Invoker(value = "getControllerBlockPos", remap = false)
    BlockPos vs_addition$getControllerBlockPos();

    @Invoker(value = "setPitch", remap = false)
    void vs_addition$setPitch(float pitch);

    @Invoker(value = "setYaw", remap = false)
    void vs_addition$setYaw(float yaw);
}
