package io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.chassis.sticker

import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.context.constraint.ConstraintGroup
import io.github.xiewuzhiying.vs_addition.context.constraint.ConstraintManager
import io.github.xiewuzhiying.vs_addition.networking.create.sticker.StickerSoundPacketS2CPacket
import io.github.xiewuzhiying.vs_addition.util.*
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
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
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.isTickingChunk
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.toWorldCoordinates
import org.valkyrienskies.mod.util.logger

open class StickerConstraintManager(val level: ServerLevel, val ship: ServerShip?, val blockPos: BlockPos, val getFacing: () -> Direction, override val core: ServerShipWorldCore = level.shipObjectWorld) : ConstraintManager(core) {
    open val bodyId: ShipId?
        get() = this.ship?.id ?: core.dimensionToGroundBodyIdImmutable[level.dimensionId]
    open val checkPos: Vector3dc
        get() = this.blockPos.centerJOMLD.add(getFacing().normal.toVector3d.mul(0.5625))

    open fun createStickerConstraint() {
        if (this.bodyId == null) return
        val checkPos = this.level.toWorldCoordinates(Vector3d(this.checkPos))
        var shouldPlaySound = false
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

            val compliance = VSAdditionConfig.SERVER.create.stickerCompliance
            val maxForce = VSAdditionConfig.SERVER.create.stickerMaxForce

            val attachmentConstraint = VSAttachmentConstraint(bodyId!!, otherId, compliance, localPos0, localPos1, maxForce, 0.0)

            val fixOrientationConstraint = VSFixedOrientationConstraint(
                bodyId!!, otherId, compliance,
                (ship?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()),
                (otherShip?.transform?.shipToWorldRotation ?: Quaterniond()).invert(Quaterniond()),
                maxForce,
            )

            this.addConstraintGroup(StickerConstraintGroup(this.createConstraint(attachmentConstraint) ?: return@transformFromWorldToNearbyLoadedShipsAndWorld, this.createConstraint(fixOrientationConstraint) ?: return@transformFromWorldToNearbyLoadedShipsAndWorld, pos.toBlockPos))
            shouldPlaySound = true
        }
        if (shouldPlaySound) {
            StickerSoundPacketS2CPacket(blockPos, true).sendToPlayers(level.players())
        }
    }

    override fun onRemoveAllConstraintGroups(map: Int2ObjectOpenHashMap<ConstraintGroup>) {
        if (map.size > 0) {
            StickerSoundPacketS2CPacket(blockPos, false).sendToPlayers(level.players())
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