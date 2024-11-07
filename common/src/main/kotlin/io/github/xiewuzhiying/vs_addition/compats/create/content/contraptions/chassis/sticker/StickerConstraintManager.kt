package io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.chassis.sticker

import io.github.xiewuzhiying.vs_addition.stuff.constraint.ConstraintGroup
import io.github.xiewuzhiying.vs_addition.stuff.constraint.ConstraintManager
import io.github.xiewuzhiying.vs_addition.util.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids
import org.joml.Quaterniond
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore
import org.valkyrienskies.core.util.expand
import org.valkyrienskies.mod.common.*
import org.valkyrienskies.mod.util.logger

open class StickerConstraintManager(val level: ServerLevel, val ship: ServerShip?, val blockPos: BlockPos, val getFacing: () -> Direction, override val core: ServerShipWorldCore = level.shipObjectWorld) : ConstraintManager(core) {
    open val bodyId: ShipId?
        get() = this.ship?.id ?: core.dimensionToGroundBodyIdImmutable[level.dimensionId]
    open val checkPos: Vector3dc
        get() = this.blockPos.centerJOMLD.add(getFacing().normal.toVector3d.mul(0.5625))

    open fun createStickerConstraint() {
        if (this.bodyId == null) return
        val checkPos = this.level.toWorldCoordinates(Vector3d(this.checkPos))
        this.level.transformFromWorldToNearbyLoadedShipsAndWorld(AABBd(checkPos, checkPos).expand(0.25)) { aabb ->
            val pos = aabb.center(Vector3d())

            val otherShip = level.getShipManagingPos2(pos)
            val otherId = otherShip?.id ?: level.shipObjectWorld.dimensionToGroundBodyIdImmutable[level.dimensionId] ?: return@transformFromWorldToNearbyLoadedShipsAndWorld
            if (otherId == this.bodyId) return@transformFromWorldToNearbyLoadedShipsAndWorld

            if (isAirOrFluid(level.getBlockState(pos.toBlockPos))) return@transformFromWorldToNearbyLoadedShipsAndWorld


            val otherPos = Vector3d(pos)
            otherShip?.transform?.worldToShip?.transformPosition(otherPos)


            val localPos0 : Vector3dc?
            val localPos1 : Vector3dc?
            when {
                this.ship == null -> {
                    localPos0 = otherShip!!.transform.positionInWorld
                    localPos1 = otherShip.transform.positionInShip
                }
                otherShip == null -> {
                    localPos0 = this.ship.transform.positionInShip
                    localPos1 = this.ship.transform.positionInWorld
                }
                else -> {
                    localPos0 = this.ship.transform.worldToShip.transformPosition(otherShip.transform.positionInWorld, Vector3d())!!
                    localPos1 = otherShip.transform.positionInShip
                }
            }

            val attachmentConstraint = VSAttachmentConstraint(bodyId!!, otherId, 1e-10, localPos0, localPos1, 1e10, 0.0)

            val fixOrientationConstraint = VSFixedOrientationConstraint(
                bodyId!!, otherId, 1e-10,
                (ship?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()),
                (otherShip?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()),
                1e10
            )

            this.addConstraintGroup(StickerConstraintGroup(this.createConstraint(attachmentConstraint) ?: return@transformFromWorldToNearbyLoadedShipsAndWorld, this.createConstraint(fixOrientationConstraint) ?: return@transformFromWorldToNearbyLoadedShipsAndWorld, pos.toBlockPos))
        }
    }

    open fun checkStickerConstraint() {
        this.constraintGroups.forEach { (id, group) ->
            group as StickerConstraintGroup
            val blockPos = group.blockPos
            if (this.level.isTickingChunk(ChunkPos(blockPos)) && isAirOrFluid(this.level.getBlockState(blockPos)) || this.level.squaredDistanceBetweenInclShips(this.blockPos, blockPos) >= 128.0) {
                this.removeConstraintGroup(id)
            }
        }
    }

    override fun getCompoundTag(group: ConstraintGroup): CompoundTag {
        return (group as StickerConstraintGroup).compoundTag
    }

    override fun createFormCompoundTag(tag: CompoundTag): ConstraintGroup {
        return StickerConstraintGroup(tag)
    }

    companion object {
        val logger by logger()
        fun isAirOrFluid(state: BlockState): Boolean {
            return state.isAir || state.fluidState != Fluids.EMPTY.defaultFluidState()
        }
    }
}