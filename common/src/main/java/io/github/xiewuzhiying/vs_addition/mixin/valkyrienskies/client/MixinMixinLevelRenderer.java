package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketClient;
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
                if (FakeAirPocketClient.INSTANCE.checkIfPointInAirPocket(position)) {
                    return null;
                }
            }
        }
        return original.call(options, force, decreased, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Unique
    private static final Set<ParticleType<?>> vs_addition$bubbleLikeParticle = Set.of(
            ParticleTypes.UNDERWATER,
            ParticleTypes.BUBBLE,
            ParticleTypes.BUBBLE_POP,
            ParticleTypes.BUBBLE_COLUMN_UP
    );

}