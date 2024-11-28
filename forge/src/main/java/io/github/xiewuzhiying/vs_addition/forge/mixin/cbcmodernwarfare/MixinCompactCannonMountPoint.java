package io.github.xiewuzhiying.vs_addition.forge.mixin.cbcmodernwarfare;

import com.llamalad7.mixinextras.sugar.Local;
import com.tterrag.registrate.util.entry.ItemEntry;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Restriction(
        conflict = {
                @Condition(value = "cbcmodernwarfare", versionPredicates = "0.0.5f+mc.1.20.1-forge")
        }
)
@Mixin(targets = "riftyboi.cbcmodernwarfare.cannon_control.compact_mount.CompactCannonMountPoint")
public abstract class MixinCompactCannonMountPoint {
    @Shadow(remap = false)
    private static int getLoadingCooldown() {
        throw new AssertionError();
    }

    @Unique
    private static ItemEntry<Item> EMPTY_MEDIUMCANNON_CARTRIDGE = null;

    @Inject(
            method = "mediumcannonInsert",
            at = @At(
                    value = "INVOKE",
                    target = "Lriftyboi/cbcmodernwarfare/cannons/medium_cannon/breech/MediumcannonBreechBlockEntity;canBeAutomaticallyLoaded()Z",
                    shift = At.Shift.AFTER
            ),
            cancellable = true,
            remap = false
    )
    private static void mediumcannonInsert(ItemStack stack, boolean simulate, @Coerce Object mediumcannon, @Coerce Object poce, CallbackInfoReturnable<ItemStack> cir, @Local(index = 5) Object breech) {
        Item item2 = ((MediumcannonBreechBlockEntityAccessor)breech).vs_addition$getInputBuffer().getItem();
        if (getEmptyMediumCannonCartridge().equals(item2)) {
            if (!simulate) {
                ((MediumcannonBreechBlockEntityAccessor)breech).vs_addition$setInputBuffer(stack);
                ((MediumcannonBreechBlockEntityAccessor)breech).vs_addition$setLoadingCooldown(getLoadingCooldown());
            }
            stack.setCount(1);
            cir.setReturnValue(new ItemStack(item2));
        }
    }

    @Unique
    private static ItemEntry<Item> getEmptyMediumCannonCartridge() {
        if (EMPTY_MEDIUMCANNON_CARTRIDGE != null) return EMPTY_MEDIUMCANNON_CARTRIDGE;
        try {
            EMPTY_MEDIUMCANNON_CARTRIDGE = (ItemEntry<Item>) Class.forName("riftyboi.cbcmodernwarfare.index.CBCModernWarfareItem").getDeclaredField("EMPTY_MEDIUMCANNON_CARTRIDGE").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {

        }
        return EMPTY_MEDIUMCANNON_CARTRIDGE;
    }

}
