package io.github.xiewuzhiying.vs_addition.compats.createbigcannons

import com.simibubi.create.content.contraptions.OrientedContraptionEntity
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.mixin.minecraft.EntityAccessor
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toMinecraft
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

        val ship = entity.level().getShipObjectManagingPos(entity.anchorVec.toJOML())
        if (ship != null) {
            instance as EntityAccessor
            ship as ServerShip

            val vec3 = (Vec3(x, y, z)).normalize().add(
                instance.random.triangle(0.0, 0.0172275 * inaccuracy.toDouble()),
                instance.random.triangle(0.0, 0.0172275 * inaccuracy.toDouble()),
                instance.random.triangle(0.0, 0.0172275 * inaccuracy.toDouble())
            ).scale(velocity.toDouble())

            if (enableCannonRecoil) {
                val applier = ship.getAttachment(GameTickForceApplier::class.java)
                val recoilForce: Double = velocity * force
                applier!!.applyInvariantForceToPos(
                    ship.transform.shipToWorldRotation.transform(vec3.toJOML().negate().normalize()).mul(recoilForce),
                    entity.anchorVec.add(0.5, 0.5, 0.5).toJOML().sub(ship.transform.positionInShip)
                )
            }

            if (addShipVelocity) {
                val vec32 = ship.velocity.toMinecraft().scale(0.05).add(vec3)
                instance.deltaMovement = vec32
                val d = vec3.horizontalDistance()
                instance.yRot = (Mth.atan2(vec3.x, vec3.z) * 57.2957763671875).toFloat()
                instance.xRot = (Mth.atan2(vec3.y, d) * 57.2957763671875).toFloat()
                instance.yRotO = instance.yRot
                instance.xRotO = instance.xRot
            }
        } else {
            originalFunction(instance, x, y, z, velocity, inaccuracy)
        }

    }
}