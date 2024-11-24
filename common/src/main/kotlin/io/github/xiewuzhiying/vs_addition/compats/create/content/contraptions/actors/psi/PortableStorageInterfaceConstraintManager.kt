package io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock
import io.github.xiewuzhiying.vs_addition.context.constraint.ConstraintGroup
import io.github.xiewuzhiying.vs_addition.context.constraint.ConstraintManager
import io.github.xiewuzhiying.vs_addition.util.centerJOMLD
import io.github.xiewuzhiying.vs_addition.util.getBodyId
import io.github.xiewuzhiying.vs_addition.util.toVector3d
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import org.joml.Quaterniond
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint
import org.valkyrienskies.core.apigame.constraints.VSSlideConstraint
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore
import kotlin.math.abs


open class PortableStorageInterfaceConstraintManager(open val controller: PortableStorageInterfaceWithShipController, core: ServerShipWorldCore) : ConstraintManager(core) {

    open fun createPSIConstraint() {
        val connected = controller.other ?: return
        val level = controller.be.level
        if (level == null || level.isClientSide()) return
        level as ServerLevel

        val be0 = controller.be
        val be1 = connected.be

        val blockPos0 = be0.blockPos
        val blockPos1 = be1.blockPos

        val blockState0 = be0.blockState
        val blockState1 = be1.blockState

        val facing0 = blockState0.getValue(PortableStorageInterfaceBlock.FACING)
        val facing1 = blockState1.getValue(PortableStorageInterfaceBlock.FACING)

        /*
        val ship0 = level.getShipManagingPos(blockPos0)
        val ship1 = level.getShipManagingPos(blockPos1)

        val quatArray0 = facing0.getRotationArray()
        val quatArray1 = facing1.opposite.getRotationArray()

        val quatArrayCopy0 = quatArray0.mapToArray { Quaterniond(it) }
        val quatArrayCopy1 = quatArray1.mapToArray { Quaterniond(it) }


        ship0?.transform?.shipToWorldRotation?.let { quatArrayCopy0.forEach { quat-> quat.mul(it) } }
        ship1?.transform?.shipToWorldRotation?.let { quatArrayCopy1.forEach { quat-> quat.mul(it) } }

        val pair = findMostOppositeQuaternionIndexes(quatArrayCopy0, quatArrayCopy1)
        */

        val body0 = level.getBodyId(blockPos0)
        val body1 = level.getBodyId(blockPos1)

        val hinge = VSFixedOrientationConstraint(body0, body1, 1e-10, Quaterniond(facing0.rotation), Quaterniond(facing1.opposite.rotation), 1e10).createConstraint() ?: return

        val localPos0 = blockPos0.centerJOMLD.add(toVector3d(facing0.step()).mul(1.25))
        val localPos1 = blockPos1.centerJOMLD.add(toVector3d(facing1.step()).mul(1.25))
        val slide = VSSlideConstraint(body0, body1, 1e-10, localPos0, localPos1, 1e10, facing0.toVector3d, 0.5).createConstraint() ?: return
        //val posDamping = VSPosDampingConstraint(body0, body1, 1e-10, localPos0, localPos1, 1e10, 0.8).createConstraint() ?: return

        this.addConstraintGroup(ConstraintGroup(listOf(hinge, slide)))
    }

    companion object {
        fun findMostOppositeQuaternionIndexes(array1: Array<Quaterniond>, array2: Array<Quaterniond>): Pair<Int, Int> {
            var mostOppositePair: Pair<Int, Int>? = null
            var minDot = Double.MAX_VALUE

            val q1 = Quaterniond()
            val q2 = Quaterniond()
            for (i in array1.indices) {
                q1.set(array1[i])
                for (j in array1.indices) {
                    q2.set(array2[j])
                    val dot = abs(q1.dot(q2))
                    if (dot < minDot) {
                        minDot = dot
                        mostOppositePair = Pair(i, j)
                    }
                }
            }

            return mostOppositePair ?: throw IllegalArgumentException("Must not be empty")
        }

        fun Direction.getRotationArray(): Array<Quaterniond> {
            return when (this) {
                Direction.DOWN -> arrayOf(
                    Quaterniond().rotationX(Math.PI),
                    Quaterniond().rotationXYZ(Math.PI, Math.PI / 2, 0.0),
                    Quaterniond().rotationXYZ(Math.PI, Math.PI, 0.0),
                    Quaterniond().rotationXYZ(Math.PI, -Math.PI / 2, 0.0)
                )

                Direction.UP -> arrayOf(
                    Quaterniond(),
                    Quaterniond().rotationY(Math.PI / 2),
                    Quaterniond().rotationY(Math.PI),
                    Quaterniond().rotationY(-Math.PI / 2)
                )

                Direction.NORTH -> arrayOf(
                    Quaterniond().rotationXYZ(Math.PI / 2, 0.0, Math.PI),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI / 2, Math.PI),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI, Math.PI),
                    Quaterniond().rotationXYZ(Math.PI / 2, -Math.PI / 2, Math.PI)
                )

                Direction.SOUTH -> arrayOf(
                    Quaterniond().rotationX(Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI / 2, 0.0),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI, 0.0),
                    Quaterniond().rotationXYZ(Math.PI / 2, -Math.PI / 2, 0.0)
                )

                Direction.WEST -> arrayOf(
                    Quaterniond().rotationXYZ(Math.PI / 2, 0.0, Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI / 2, Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI, Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, -Math.PI / 2, Math.PI / 2)
                )

                Direction.EAST -> arrayOf(
                    Quaterniond().rotationXYZ(Math.PI / 2, 0.0, -Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI / 2, -Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, Math.PI, -Math.PI / 2),
                    Quaterniond().rotationXYZ(Math.PI / 2, -Math.PI / 2, -Math.PI / 2),
                )
            }
        }
    }
}
