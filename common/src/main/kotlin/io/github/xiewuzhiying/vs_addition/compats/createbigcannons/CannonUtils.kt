package io.github.xiewuzhiying.vs_addition.compats.createbigcannons

import com.simibubi.create.content.contraptions.OrientedContraptionEntity
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.mixin.minecraft.EntityAccessor
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import org.valkyrienskies.mod.common.util.toJOML
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile

object CannonUtils {
    @JvmStatic
    fun modify(
        instance: AbstractCannonProjectile,
        x: Double, y: Double, z: Double,
        velocity: Float, inaccuracy: Float,
        entity: OrientedContraptionEntity,
        force: Double,
        originalFunction: (AbstractCannonProjectile, Double, Double, Double, Float, Float) -> Void) {
        val enableCannonRecoil = VSAdditionConfig.SERVER.createBigCannons.enableCannonRecoil
        val addShipVelocity = VSAdditionConfig.SERVER.createBigCannons.addShipVelocity
        if (!enableCannonRecoil && !addShipVelocity) {
            originalFunction(instance, x, y, z, velocity, inaccuracy)
            return
        }

        val ship = entity.level().getShipObjectManagingPos(entity.anchorVec.toJOML()) ?: run {
            originalFunction(instance, x, y, z, velocity, inaccuracy)
            return
        }

        instance as EntityAccessor
        ship as ServerShip

        val vec3 = (Vec3(x, y, z)).normalize().add(
            instance.random.triangle(0.0, 0.0172275 * inaccuracy.toDouble()),
            instance.random.triangle(0.0, 0.0172275 * inaccuracy.toDouble()),
            instance.random.triangle(0.0, 0.0172275 * inaccuracy.toDouble())
        ).scale(velocity.toDouble())

        if (enableCannonRecoil) {
            val applier = ship.getAttachment(GameTickForceApplier::class.java)
            if (applier != null) {
                val recoilForce: Double = velocity * force
                applier.applyInvariantForceToPos(
                    ship.transform.shipToWorldRotation.transform(vec3.toJOML().negate().normalize()).mul(recoilForce),
                    entity.anchorVec.add(0.5, 0.5, 0.5).toJOML().sub(ship.transform.positionInShip)
                )
            }
        }

        if (addShipVelocity) {
            val shipVelocity = ship.velocity.mul(0.05, Vector3d())
            val r = entity.anchorVec.toJOML().sub(ship.transform.positionInShip)
            val comb = vec3.scale(1 + shipVelocity.add((ship.omega).cross(r, Vector3d())).dot(vec3.toJOML()))
            instance.deltaMovement = comb
            val d = vec3.horizontalDistance()
            instance.yRot = (Mth.atan2(vec3.x, vec3.z) * 57.2957763671875).toFloat()
            instance.xRot = (Mth.atan2(vec3.y, d) * 57.2957763671875).toFloat()
            instance.yRotO = instance.yRot
            instance.xRotO = instance.xRot
        } else {
            originalFunction(instance, x, y, z, velocity, inaccuracy)
        }

    }
}