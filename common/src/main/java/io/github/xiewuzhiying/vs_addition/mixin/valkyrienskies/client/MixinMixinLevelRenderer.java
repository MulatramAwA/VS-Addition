package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient;
import io.github.xiewuzhiying.vs_addition.util.ShipUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixin.feature.transform_particles.MixinParticle;

import java.util.Set;

@Pseudo
@Mixin(LevelRenderer.class)
public abstract class MixinMixinLevelRenderer {
    @Shadow @Nullable private ClientLevel level;

    /**
     * Render particles in-world. The {@link MixinParticle} is not sufficient because this method includes a distance
     * check, but this mixin is also not sufficient because not every particle is spawned using this method.
     */
    @WrapMethod(
            method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;"
    )
    private Particle spawnParticleInWorld(ParticleOptions options, boolean force, boolean decreased, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Operation<Particle> original) {
        if (VSAdditionConfig.COMMON.getExperimental().getFakeAirPocket()) {
            final ParticleType<?> type = options.getType();
            if (waterLikeParticle.contains(type)) {
                final Vector3d position = VSGameUtilsKt.toWorldCoordinates(this.level, x, y, z);
                if (FakeAirPocketClient.INSTANCE.checkIfPointInAirPocket(position)) {
                    return null;
                }
            }
        }
        return ShipUtils.inclFakeAirPocket(options, force, decreased, x, y, z, xSpeed, ySpeed, zSpeed, original::call, this.level);
    }

    @Unique
    private static Set<ParticleType<?>> waterLikeParticle =
            Set.of(
                    ParticleTypes.UNDERWATER,
                    ParticleTypes.BUBBLE,
                    ParticleTypes.BUBBLE_POP,
                    ParticleTypes.BUBBLE_COLUMN_UP
            );

}