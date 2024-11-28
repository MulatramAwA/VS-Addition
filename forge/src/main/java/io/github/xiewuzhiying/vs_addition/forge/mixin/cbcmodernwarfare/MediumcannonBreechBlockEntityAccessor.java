package io.github.xiewuzhiying.vs_addition.forge.mixin.cbcmodernwarfare;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "riftyboi.cbcmodernwarfare.cannons.medium_cannon.breech.MediumcannonBreechBlockEntity")
public interface MediumcannonBreechBlockEntityAccessor {
    @Invoker(value = "getInputBuffer", remap = false)
    ItemStack vs_addition$getInputBuffer();

    @Invoker(value = "setInputBuffer", remap = false)
    void vs_addition$setInputBuffer(ItemStack stack);

    @Invoker(value = "setLoadingCooldown", remap = false)
    void vs_addition$setLoadingCooldown(int cooldown);
}
