package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketClient;
import io.github.xiewuzhiying.vs_addition.mixinducks.valkyrienskies.ParticleMixinDuck;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Set;

@Pseudo
@Mixin(value = LevelRenderer.class, priority = 1200)
public abstract class MixinMixinLevelRenderer {
    @Shadow @Nullable private ClientLevel level;

    @WrapMethod(
            method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;"
    )
    private Particle spawnParticleInWorld(ParticleOptions options, boolean force, boolean decreased, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Operation<Particle> original) {
        if (VSAdditionConfig.COMMON.getExperimental().getFakeAirPocket() && VSAdditionConfig.CLIENT.getExperimental().getRemoveBubbleLikeParticlesInFakeAirPocket()) {
            final ParticleType<?> type = options.getType();
            if (vs_addition$bubbleLikeParticle.contains(type)) {
                final Vector3d position = VSGameUtilsKt.toWorldCoordinates(this.level, x, y, z);
                if (FakeAirPocketClient.checkIfPointInAirPocket(position)) {
                    return null;
                }
            }
        }
        final ClientShip ship = (ClientShip) VSGameUtilsKt.getShipManagingPos(level, ((int)x) >> 4, ((int)z) >> 4);

        if (ship == null) {
            // vanilla behaviour
            return original.call(options, force, decreased, x, y, z, xSpeed, ySpeed, zSpeed);
        }

        final Matrix4dc transform = ship.getRenderTransform().getShipToWorld();

        // in-world position
        final Vector3d p = transform.transformPosition(new Vector3d(x, y, z));

        // in-world velocity
        final Vector3d v = transform // Rotate velocity wrt ship transform
                .transformDirection(
                        new Vector3d(
                                xSpeed,
                                ySpeed,
                                zSpeed
                        )
                ) // Tack on the ships linear velocity (multiplied by 1/20 because particle velocity is given per tick)
                .fma(0.05, ship.getVelocity());

        // Return and re-call this method with new coords
        final Particle particle = original.call(options, force, decreased, p.x, p.y, p.z, v.x, v.y, v.z);
        if (particle != null && particle instanceof ParticleMixinDuck duck) {
            duck.vs_addition$setOriginalPosition(new Vector3d(x, y, z));
            duck.vs_addition$setShip(ship);
            duck.vs_addition$setFirstTimeScale(transform.getScale(new Vector3d()).z);
        }
        return particle;
    }

    @Unique
    private static final Set<ParticleType<?>> vs_addition$bubbleLikeParticle = Set.of(
            ParticleTypes.UNDERWATER,
            ParticleTypes.BUBBLE,
            ParticleTypes.BUBBLE_POP,
            ParticleTypes.BUBBLE_COLUMN_UP
    );

}