package io.github.xiewuzhiying.vs_addition.forge.mixin.valkyrienskies.water;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocket;
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketClient;
import kotlin.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.joml.Vector3d;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Set;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    private Level level;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getZ();

    @Shadow public abstract double getEyeY();

    @Shadow @Final private Set<TagKey<Fluid>> fluidOnEyes;

    @Shadow public abstract void setSwimming(boolean swimming);

    @Shadow protected boolean wasEyeInWater;
    @Shadow protected boolean wasTouchingWater;
    @Shadow(remap = false) private FluidType forgeFluidTypeOnEyes;
    @Unique
    private static EntityPolygonCollider collider = null;
    @Unique
    private boolean isTouchFakeAirPocket = false;
    @Unique
    private boolean isEyeInFakeAirPocket = false;

    @Inject(
            method = "baseTick",
            at = @At("HEAD")
    )
    private void baseTick(CallbackInfo ci) {
        if (VSAdditionConfig.COMMON.getExperimental().getFakeAirPocket() && this.level != null) {
            this.isEyeInFakeAirPocket = false;
            this.isTouchFakeAirPocket = false;
            Level level = this.level;
            if (level != null) {
                if (level instanceof ServerLevel serverLevel) {
                    final AABBdc aabb = VectorConversionsMCKt.toJOML(this.getBoundingBox());
                    Pair<Boolean, Boolean> pair = FakeAirPocket.checkIfPointAndAABBInAirPocket(new Vector3d(this.getX(), this.getEyeY() - 0.1111111119389534, this.getZ()), aabb, serverLevel, true, aabb);
                    this.isEyeInFakeAirPocket = pair.getFirst();
                    this.isTouchFakeAirPocket = pair.getSecond();
                } else {
                    final AABBdc aabb = VectorConversionsMCKt.toJOML(this.getBoundingBox());
                    Pair<Boolean, Boolean> pair = FakeAirPocketClient.INSTANCE.checkIfPointAndAABBInAirPocket(new Vector3d(this.getX(), this.getEyeY() - 0.1111111119389534, this.getZ()), aabb, true, aabb);
                    this.isEyeInFakeAirPocket = pair.getFirst();
                    this.isTouchFakeAirPocket = pair.getSecond();
                }
            }
        }
    }

    @WrapMethod(
            method = "updateFluidHeightAndDoFluidPushing()V",
            remap = false
    )
    private void onUpdateFluidHeightAndDoFluidPushing(Operation<Void> original) {
        if (this.isTouchFakeAirPocket) {
            this.wasTouchingWater = false;
            return;
        }
        original.call();
    }

    @WrapMethod(
            method = "updateFluidOnEyes"
    )
    private void onUpdateFluidOnEyes(Operation<Void> original) {
        if (this.isEyeInFakeAirPocket) {
            this.wasEyeInWater = false;
            this.fluidOnEyes.clear();
            this.forgeFluidTypeOnEyes = ForgeMod.EMPTY_TYPE.get();
            return;
        }
        original.call();
    }

    @WrapMethod(
            method = "updateSwimming"
    )
    private void onUpdateSwimming(Operation<Void> original) {
        if (this.isTouchFakeAirPocket) {
            this.setSwimming(false);
            return;
        }
        original.call();
    }

    @WrapMethod(
            method = "isInBubbleColumn"
    )
    private boolean onIsInBubbleColumn(Operation<Boolean> original) {
        return !this.isTouchFakeAirPocket && original.call();
    }

    @Unique
    private static EntityPolygonCollider getCollider() {
        if (collider == null) {
            collider = ValkyrienSkiesMod.vsCore.getEntityPolygonCollider();
        }
        return collider;
    }
}
