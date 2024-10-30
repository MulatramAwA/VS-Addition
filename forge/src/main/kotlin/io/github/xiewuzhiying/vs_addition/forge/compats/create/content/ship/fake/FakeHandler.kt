package io.github.xiewuzhiying.vs_addition.forge.compats.create.content.ship.fake

import com.simibubi.create.AllItems
import com.simibubi.create.AllPackets
import com.simibubi.create.content.contraptions.BlockMovementChecks
import com.simibubi.create.content.contraptions.glue.GlueEffectPacket
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity
import com.simibubi.create.content.contraptions.glue.SuperGlueItem
import com.simibubi.create.foundation.placement.IPlacementHelper
import com.simibubi.create.foundation.utility.Iterate
import com.simibubi.create.foundation.utility.worldWrappers.RayTraceWorld
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.HitResult
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent
import net.minecraftforge.network.PacketDistributor
import org.joml.primitives.AABBdc


object FakeHandler {

    @JvmStatic
    fun glueListensForBlockPlacement(event: EntityPlaceEvent) {
        val world = event.level
        val entity = event.entity
        val pos = event.pos

        if (entity == null || world == null || pos == null) return
        if (world.isClientSide) return

        val cached: Set<SuperGlueEntity> = HashSet()
        for (direction in Iterate.directions) {
            val relative = pos.relative(direction)
            if (SuperGlueEntity.isGlued(world, pos, direction, cached)
                && BlockMovementChecks.isMovementNecessary(world.getBlockState(relative), entity.level(), relative)
            ) AllPackets.getChannel().send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with { entity },
                GlueEffectPacket(pos, direction, true)
            )
        }

        if (entity is Player) glueInOffHandAppliesOnBlockPlace(event, pos, entity)
    }

    @JvmStatic
    fun glueInOffHandAppliesOnBlockPlace(event: EntityPlaceEvent, pos: BlockPos, placer: Player) {
        val itemstack = placer.offhandItem
        val reachAttribute = placer.getAttribute(ForgeMod.BLOCK_REACH.get())
        if (!AllItems.SUPER_GLUE.isIn(itemstack) || reachAttribute == null) return
        if (AllItems.WRENCH.isIn(placer.mainHandItem)) return
        if (event.placedAgainst === IPlacementHelper.ID) return

        val distance = reachAttribute.value
        val start = placer.getEyePosition(1f)
        val look = placer.getViewVector(1f)
        val end = start.add(look.x * distance, look.y * distance, look.z * distance)
        val world = placer.level()

        val rayTraceWorld =
            RayTraceWorld(
                world
            ) { p: BlockPos, state: BlockState? -> if (p == pos) Blocks.AIR.defaultBlockState() else state }
        val ray =
            rayTraceWorld.clip(ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer))

        val face = ray.direction
        if (face == null || ray.type == HitResult.Type.MISS) return

        val gluePos = ray.blockPos
        if (gluePos.relative(face)
            != pos
        ) {
            event.isCanceled = true
            return
        }

        if (SuperGlueEntity.isGlued(world, gluePos, face, null)) return

        val entity = SuperGlueEntity(world, SuperGlueEntity.span(gluePos, gluePos.relative(face)))
        val compoundnbt = itemstack.tag
        if (compoundnbt != null) EntityType.updateCustomEntityTag(world, placer, entity, compoundnbt)

        if (SuperGlueEntity.isValidFace(world, gluePos, face)) {
            if (!world.isClientSide) {
                world.addFreshEntity(entity)
                AllPackets.getChannel().send(
                    PacketDistributor.TRACKING_ENTITY.with { entity },
                    GlueEffectPacket(gluePos, face, true)
                )
            }
            itemstack.hurtAndBreak(
                1, placer
            ) { player: Player? ->
                SuperGlueItem.onBroken(
                    player
                )
            }
        }
    }
}