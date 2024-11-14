package io.github.xiewuzhiying.vs_addition.context

import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.impl.game.BlockTypeImpl
import org.valkyrienskies.mod.common.BlockStateInfo
import org.valkyrienskies.mod.common.BlockStateInfoProvider
import org.valkyrienskies.physics_api.Lod1BlockStateId
import org.valkyrienskies.physics_api.Lod1LiquidBlockStateId
import org.valkyrienskies.physics_api.Lod1SolidBlockStateId
import org.valkyrienskies.physics_api.voxel.Lod1LiquidBlockState
import org.valkyrienskies.physics_api.voxel.Lod1SolidBlockState

class NonColliderBlockStateProvider : BlockStateInfoProvider {

    override val blockStateData: List<Triple<Lod1SolidBlockStateId, Lod1LiquidBlockStateId, Lod1BlockStateId>>
        get() = throw NotImplementedError()
    override val liquidBlockStates: List<Lod1LiquidBlockState>
        get() = throw NotImplementedError()
    override val priority: Int
        get() = VSAdditionConfig.SERVER.nonColliderBlocksPriority
    override val solidBlockStates: List<Lod1SolidBlockState>
        get() = throw NotImplementedError()

    override fun getBlockStateMass(blockState: BlockState): Double? {
        return null
    }

    override fun getBlockStateType(blockState: BlockState): BlockType? {
        return if (VSAdditionConfig.SERVER.nonColliderBlocks.contains((blockState.block.`arch$registryName`() ?: return null).toString())) {
            BlockTypeImpl.a.AIR
        } else {
            null
        }
    }

    companion object {
        @JvmStatic
        fun register() {
            Registry.register(
                BlockStateInfo.REGISTRY,
                ResourceLocation(VSAdditionMod.MOD_ID, "data"),
                NonColliderBlockStateProvider()
            )
        }
    }
}