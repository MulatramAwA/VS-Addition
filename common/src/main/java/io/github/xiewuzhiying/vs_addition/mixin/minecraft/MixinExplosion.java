package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.architectury.platform.Platform;
import io.github.xiewuzhiying.vs_addition.context.conditiontester.ExplosionConditionTester;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.util.AABBdUtilKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.VSGameConfig;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

import java.util.*;

import static io.github.xiewuzhiying.vs_addition.util.ShipUtilsKt.getLoadedShipsIntersecting;


@Pseudo
@Restriction(
        require = @Condition(type = Condition.Type.TESTER, tester = ExplosionConditionTester.class)
)
@Mixin(Explosion.class)
public abstract class MixinExplosion {

    @Mutable
    @Shadow @Final private double x;

    @Shadow @Final private Level level;

    @Mutable
    @Shadow @Final private double y;

    @Mutable
    @Shadow @Final private double z;

    @Shadow public float radius;

    @Shadow @Final private ExplosionDamageCalculator damageCalculator;

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;)V",
            at = @At("TAIL")
    )
    private void toWorld(final Level level, final Entity source, final DamageSource damageSource, final ExplosionDamageCalculator damageCalculator, final double toBlowX, final double toBlowY, final double toBlowZ, final float radius, final boolean fire, final Explosion.BlockInteraction blockInteraction, final CallbackInfo ci) {
        final Vector3d vector3d = VSGameUtilsKt.toWorldCoordinates(this.level, new Vector3d(this.x, this.y, this.z));
        this.x = vector3d.x;
        this.y = vector3d.y;
        this.z = vector3d.z;
    }

    @Inject(
            method = "explode",
            at = @At("HEAD")
    )
    private void getShips(final CallbackInfo ci, @Share("pairs") final LocalRef<Iterable<ImmutablePair<LoadedShip, GameTickForceApplier>>> pairs) {
        final List<ImmutablePair<LoadedShip, GameTickForceApplier>> list = new ArrayList<>();
        final Iterable<LoadedShip> ships = getLoadedShipsIntersecting(this.level, AABBdUtilKt.expand(new AABBd(this.x, this.y, this.z, this.x, this.y, this.z), this.radius));
        for (LoadedShip ship : ships) {
            if (this.level.isClientSide()) {
                list.add(ImmutablePair.of(ship, null));
                continue;
            }
            if (ship instanceof LoadedServerShip serverShip) {
                GameTickForceApplier applier = serverShip.getAttachment(GameTickForceApplier.class);
                if (applier == null) {
                    applier = new GameTickForceApplier();
                    serverShip.setAttachment(GameTickForceApplier.class, applier);
                }
                list.add(ImmutablePair.of(serverShip, applier));
            }
        }
        pairs.set(Collections.unmodifiableList(list));
    }

    @ModifyExpressionValue(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;isInWorldBounds(Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean injector(final boolean original, @Local(ordinal = 4) final double m, @Local(ordinal = 5) final double n, @Local(ordinal = 6) final double o,
                             @Local(ordinal = 0) final LocalFloatRef h, @Local final Set<BlockPos> set,
                             @Share("pairs") final LocalRef<Iterable<ImmutablePair<LoadedShip, GameTickForceApplier>>> pairs) {
        boolean needToBreak = false;
        for (final ImmutablePair<LoadedShip, GameTickForceApplier> pair : pairs.get()) {
            if (!(h.get() > 0.0F)) {
                needToBreak = true;
                break;
            }
            final Matrix4dc worldToShip = pair.getLeft().getTransform().getWorldToShip();
            final Vector3d vec31 = worldToShip.transformPosition(new Vector3d(m, n, o));
            final BlockPos blockPos = ConversionUtilsKt.getToBlockPos(vec31);
            final BlockState blockState = this.level.getBlockState(blockPos);
            final FluidState fluidState = this.level.getFluidState(blockPos);

            final Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance((Explosion) (Object) this, this.level, blockPos, blockState, fluidState);
            if (optional.isPresent()) {
                final float originH = h.get();
                final float sub = (optional.get() + 0.3F) * 0.3F;
                if (!this.level.isClientSide()) {
                    final GameTickForceApplier forceApplier = pair.getRight();
                    if (forceApplier != null) {
                        final Vector3d vec32 = worldToShip.transformPosition(new Vector3d(this.x, this.y, this.z));

                        vec32.sub(vec31);
                        final double distanceMult = Math.max(0, (this.radius - vec32.length()));
                        vec32.normalize();
                        vec32.mul(distanceMult);
                        vec32.mul(VSGameConfig.SERVER.getExplosionBlastForce() * Math.min(originH, sub) * 0.000001);

                        if (vec32.isFinite()) {
                            forceApplier.applyInvariantForceToPos(vec32, vec31);
                        }
                    }
                }
                h.set(originH - sub);
            }
            ;

            if (h.get() > 0.0F && this.damageCalculator.shouldBlockExplode((Explosion) (Object) this, this.level, blockPos, blockState, h.get())) {
                set.add(blockPos);
            }
        }
        return original && !needToBreak;
    }
}
