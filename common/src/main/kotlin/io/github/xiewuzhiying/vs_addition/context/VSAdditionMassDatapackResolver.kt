package io.github.xiewuzhiying.vs_addition.context

import com.google.gson.Gson
import com.google.gson.JsonElement
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.tags.TagKey
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.VoxelShape
import org.joml.Vector3f
import org.joml.Vector3i
import org.valkyrienskies.core.apigame.world.chunks.BlockType
import org.valkyrienskies.core.game.VSBlockType
import org.valkyrienskies.core.impl.collision.Lod1SolidShapeUtils
import org.valkyrienskies.core.impl.game.BlockTypeImpl
import org.valkyrienskies.mod.common.BlockStateInfoProvider
import org.valkyrienskies.mod.common.hooks.VSGameEvents
import org.valkyrienskies.mod.common.vsCore
import org.valkyrienskies.mod.mixin.accessors.world.level.block.SlabBlockAccessor
import org.valkyrienskies.mod.mixin.accessors.world.level.block.StairBlockAccessor
import org.valkyrienskies.mod.util.logger
import org.valkyrienskies.physics_api.Lod1BlockStateId
import org.valkyrienskies.physics_api.Lod1LiquidBlockStateId
import org.valkyrienskies.physics_api.Lod1SolidBlockStateId
import org.valkyrienskies.physics_api.voxel.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private data class VSBlockStateInfo(
    val id: ResourceLocation,
    val priority: Int,
    val mass: Double,
    val friction: Double,
    val elasticity: Double,
    val type: VSBlockType?,
)

private data class VSFluidStateInfo(
    val id: ResourceLocation,
    val priority: Int,
    val density: Double,
    val dragCoefficient: Double,
    val type: VSBlockType?,
)
object VSAdditionMassDatapackResolver : BlockStateInfoProvider {
    private val map = hashMapOf<ResourceLocation, VSBlockStateInfo>()
    private val fluidMap = hashMapOf<ResourceLocation, VSFluidStateInfo>()
    private val _solidBlockStates: MutableList<Lod1SolidBlockState> = ArrayList()
    private val _liquidBlockStates: MutableList<Lod1LiquidBlockState> = ArrayList()
    private val _blockStateData: MutableList<Triple<Lod1SolidBlockStateId, Lod1LiquidBlockStateId, Lod1BlockStateId>> = ArrayList()
    private val blockStateToId: MutableMap<BlockState, BlockType> = HashMap()

    val loader get() = VSMassDataLoader()

    private val DEFAULT_MASS
        get() = VSAdditionConfig.SERVER.defaultBlockMass
    private val DEFAULT_ELASTICITY
        get() = VSAdditionConfig.SERVER.defaultBlockElasticity
    private val DEFAULT_FRICTION
        get() = VSAdditionConfig.SERVER.defaultBlockFriction
    private val DEFAULT_DENSITY
        get() = VSAdditionConfig.SERVER.defaultFluidDensity
    private val DEFAULT_DRAG_COEFFICIENT
        get() = VSAdditionConfig.SERVER.defaultFluidDragCoefficient
    // Unused for now, placeholder for later
    private val DEFAULT_HARDNESS
        get() = VSAdditionConfig.SERVER.defaultBlockHardness

    override val priority: Int
        get() = 100

    override fun getBlockStateMass(blockState: BlockState): Double? =
        map[BuiltInRegistries.BLOCK.getKey(blockState.block)]?.mass

    override fun getBlockStateType(blockState: BlockState): VSBlockType? =
        blockStateToId[blockState]!!

    var registeredBlocks = false
        private set

    override val solidBlockStates: List<Lod1SolidBlockState>
        get() = _solidBlockStates
    override val liquidBlockStates: List<Lod1LiquidBlockState>
        get() = _liquidBlockStates
    override val blockStateData: List<Triple<Lod1SolidBlockStateId, Lod1LiquidBlockStateId, Lod1BlockStateId>>
        get() = _blockStateData

    class VSMassDataLoader : SimpleJsonResourceReloadListener(Gson(), "vs_mass") {
        private val tags = mutableListOf<VSBlockStateInfo>()

        override fun apply(
            objects: MutableMap<ResourceLocation, JsonElement>?,
            resourceManager: ResourceManager?,
            profiler: ProfilerFiller?
        ) {
            map.clear()
            tags.clear()
            fluidMap.clear()
            objects?.forEach { (location, element) ->
                try {
                    if (element.isJsonArray) {
                        element.asJsonArray.forEach { element1: JsonElement ->
                            parse(element1, location)
                        }
                    } else if (element.isJsonObject) {
                        parse(element, location)
                    } else throw IllegalArgumentException()
                } catch (e: Exception) {
                    logger.error(e)
                }
            }
        }

        init {
            VSGameEvents.tagsAreLoaded.on { _, _ ->
                tags.forEach { tagInfo ->
                    val tag: Optional<HolderSet.Named<Block>>? =
                        BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, tagInfo.id))
                    if (tag != null) {

                        if (!tag.isPresent()) {
                            logger.warn("No specified tag '${tagInfo.id}' doesn't exist!")
                            return@forEach
                        }

                        tag.get().forEach {
                            add(
                                VSBlockStateInfo(
                                    BuiltInRegistries.BLOCK.getKey(it.value()), tagInfo.priority, tagInfo.mass, tagInfo.friction,
                                    tagInfo.elasticity, tagInfo.type
                                )
                            )
                        }
                    }
                }
            }
        }

        // so why does this exist? cus for some reason initializes their tags after all the other things
        // idk why, so we note them down and use them later
        private fun addToBeAddedTags(tag: VSBlockStateInfo) {
            tags.add(tag)
        }

        private fun add(info: VSBlockStateInfo) {
            if (map.containsKey(info.id)) {
                if (map[info.id]!!.priority < info.priority) {
                    map[info.id] = info
                }
            } else {
                map[info.id] = info
            }
        }

        private fun addFluid(info: VSFluidStateInfo) {
            if(fluidMap.containsKey(info.id)) {
                if(fluidMap[info.id]!!.priority < info.priority) {
                    fluidMap[info.id] = info
                }
            } else {
                fluidMap[info.id] = info
            }
        }

        private fun parse(element: JsonElement, origin: ResourceLocation) {
            val jsonObject = element.asJsonObject
            val tag = jsonObject["tag"]?.asString
            val block = jsonObject["block"]?.asString
            val fluid = jsonObject["fluid"]?.asString
            val priority = jsonObject["priority"]?.asInt ?: 100

            fun createBlockStateInfo(resourceLocation: ResourceLocation): VSBlockStateInfo {
                val weight = jsonObject["mass"]?.asDouble ?: DEFAULT_MASS
                val friction = jsonObject["friction"]?.asDouble ?: DEFAULT_FRICTION
                val elasticity = jsonObject["elasticity"]?.asDouble ?: DEFAULT_ELASTICITY
                return VSBlockStateInfo(resourceLocation, priority, weight, friction, elasticity, null)
            }

            when {
                tag != null -> addToBeAddedTags(createBlockStateInfo(ResourceLocation(tag)))
                block != null -> add(createBlockStateInfo(ResourceLocation(block)))
                fluid != null -> {
                    val density = jsonObject["density"]?.asDouble ?: DEFAULT_DENSITY
                    val dragCoefficient = jsonObject["dragCoefficient"]?.asDouble ?: DEFAULT_DRAG_COEFFICIENT
                    addFluid(VSFluidStateInfo(ResourceLocation(fluid), priority, density, dragCoefficient, null))
                }
                else -> throw IllegalArgumentException("No block or tag in file $origin")
            }
        }
    }

    private fun generateStairCollisionShapes(stairShapes: Array<VoxelShape>): Map<VoxelShape, Lod1SolidCollisionShape> {
        val testPoints = listOf(
            CollisionPoint(Vector3f(.25f, .25f, .25f), .25f),
            CollisionPoint(Vector3f(.25f, .25f, .75f), .25f),
            CollisionPoint(Vector3f(.25f, .75f, .25f), .25f),
            CollisionPoint(Vector3f(.25f, .75f, .75f), .25f),
            CollisionPoint(Vector3f(.75f, .25f, .25f), .25f),
            CollisionPoint(Vector3f(.75f, .25f, .75f), .25f),
            CollisionPoint(Vector3f(.75f, .75f, .25f), .25f),
            CollisionPoint(Vector3f(.75f, .75f, .75f), .25f),
        )

        val testBoxes = listOf(
            LodBlockBoundingBox.createVSBoundingBox(0, 0, 0, 7, 7, 7),
            LodBlockBoundingBox.createVSBoundingBox(0, 0, 8, 7, 7, 15),
            LodBlockBoundingBox.createVSBoundingBox(0, 8, 0, 7, 15, 7),
            LodBlockBoundingBox.createVSBoundingBox(0, 8, 8, 7, 15, 15),
            LodBlockBoundingBox.createVSBoundingBox(8, 0, 0, 15, 7, 7),
            LodBlockBoundingBox.createVSBoundingBox(8, 0, 8, 15, 7, 15),
            LodBlockBoundingBox.createVSBoundingBox(8, 8, 0, 15, 15, 7),
            LodBlockBoundingBox.createVSBoundingBox(8, 8, 8, 15, 15, 15),
        )

        val map: MutableMap<VoxelShape, Lod1SolidCollisionShape> = HashMap()
        stairShapes.forEach { stairShape ->
            val points: MutableList<CollisionPoint> = ArrayList()
            val positiveBoxes: MutableList<LodBlockBoundingBox> = ArrayList()
            val negativeBoxes: MutableList<LodBlockBoundingBox> = ArrayList()
            testPoints.forEachIndexed { index, testPoint ->
                var added = false
                stairShape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
                    if (testPoint.pos.x() in minX .. maxX && testPoint.pos.y() in minY .. maxY && testPoint.pos.z() in minZ .. maxZ) {
                        points.add(testPoint)
                        added = true
                        return@forAllBoxes
                    }
                }
                if (added) {
                    positiveBoxes.add(testBoxes[index])
                } else {
                    negativeBoxes.add(testBoxes[index])
                }
            }
            val minTotalAABB = Vector3i(positiveBoxes[0].minX.toInt(), positiveBoxes[0].minY.toInt(), positiveBoxes[0].minZ.toInt())
            val maxTotalAABB = Vector3i(positiveBoxes[0].maxX.toInt(), positiveBoxes[0].maxY.toInt(), positiveBoxes[0].maxZ.toInt())
            for (i in 1 until positiveBoxes.size) {
                minTotalAABB.x = min(minTotalAABB.x, positiveBoxes[i].minX.toInt())
                minTotalAABB.y = min(minTotalAABB.y, positiveBoxes[i].minY.toInt())
                minTotalAABB.z = min(minTotalAABB.z, positiveBoxes[i].minZ.toInt())
                maxTotalAABB.x = max(maxTotalAABB.x, positiveBoxes[i].maxX.toInt())
                maxTotalAABB.y = max(maxTotalAABB.y, positiveBoxes[i].maxY.toInt())
                maxTotalAABB.z = max(maxTotalAABB.z, positiveBoxes[i].maxZ.toInt())
            }
            val overallBox = LodBlockBoundingBox.createVSBoundingBox(
                minTotalAABB.x.toByte(), minTotalAABB.y.toByte(), minTotalAABB.z.toByte(), maxTotalAABB.x.toByte(),
                maxTotalAABB.y.toByte(), maxTotalAABB.z.toByte()
            )
            val collisionShape = Lod1SolidBoxesCollisionShape(
                overallBoundingBox = overallBox,
                collisionPoints = points,
                solidBoxes = Lod1SolidShapeUtils.mergeBoxes(positiveBoxes),
                negativeBoxes = Lod1SolidShapeUtils.mergeBoxes(negativeBoxes),
            )
            map[stairShape] = collisionShape
        }
        return map
    }

    private fun generateShapeFromVoxel(voxelShape: VoxelShape): Lod1SolidBoxesCollisionShape? {
        val posBoxes = ArrayList<LodBlockBoundingBox>()
        var failed = false
        var maxBoxesToTest = 20
        voxelShape.forAllBoxes { minX, minY, minZ, maxX, maxY, maxZ ->
            if (failed) {
                return@forAllBoxes
            }
            val lodMinX = (minX * 16).roundToInt().toByte()
            val lodMinY = (minY * 16).roundToInt().toByte()
            val lodMinZ = (minZ * 16).roundToInt().toByte()
            val lodMaxX = ((maxX * 16).roundToInt() - 1).toByte()
            val lodMaxY = ((maxY * 16).roundToInt() - 1).toByte()
            val lodMaxZ = ((maxZ * 16).roundToInt() - 1).toByte()
            if (lodMinX !in 0..15 || lodMinY !in 0..15 || lodMinZ !in 0..15 || lodMaxX !in 0..15 || lodMaxY !in 0..15 || lodMaxZ !in 0..15) {
                // Out of range
                failed = true
                return@forAllBoxes
            } else {
                posBoxes.add(
                    LodBlockBoundingBox.createVSBoundingBox(lodMinX, lodMinY, lodMinZ, lodMaxX, lodMaxY, lodMaxZ)
                )
            }
            if (maxBoxesToTest == 0) {
                failed = true
            } else {
                maxBoxesToTest--
            }
        }
        return if (!failed) {
            Lod1SolidShapeUtils.generateShapeFromBoxes(posBoxes)
        } else {
            null
        }
    }

    fun registerAllBlockStates(blockStates: Iterable<BlockState>) {
        val fullLodBoundingBox = LodBlockBoundingBox.createVSBoundingBox(0, 0, 0, 15, 15, 15)
        val fullBlockCollisionPoints = listOf(
            CollisionPoint(Vector3f(.25f, .25f, .25f), .25f),
            CollisionPoint(Vector3f(.25f, .25f, .75f), .25f),
            CollisionPoint(Vector3f(.25f, .75f, .25f), .25f),
            CollisionPoint(Vector3f(.25f, .75f, .75f), .25f),
            CollisionPoint(Vector3f(.75f, .25f, .25f), .25f),
            CollisionPoint(Vector3f(.75f, .25f, .75f), .25f),
            CollisionPoint(Vector3f(.75f, .75f, .25f), .25f),
            CollisionPoint(Vector3f(.75f, .75f, .75f), .25f),
        )
        val fullBlockCollisionShape = Lod1SolidBoxesCollisionShape(
            overallBoundingBox = fullLodBoundingBox,
            collisionPoints = fullBlockCollisionPoints,
            solidBoxes = listOf(fullLodBoundingBox),
            negativeBoxes = listOf(),
        )

        // Add default block states
        run {
            // region Add default solid block state
            val solidBlockState = Lod1SolidBlockState(
                collisionShape = fullBlockCollisionShape,
                elasticity = DEFAULT_ELASTICITY.toFloat(),
                friction = DEFAULT_FRICTION.toFloat(),
                hardness = DEFAULT_HARDNESS.toFloat(),
                lod1SolidBlockStateId = BlockTypeImpl.SOLID.toInt(),
            )
            _solidBlockStates.add(solidBlockState)
            _blockStateData.add(Triple(BlockTypeImpl.SOLID.toInt(), BlockTypeImpl.AIR.toInt(), BlockTypeImpl.SOLID.toInt()))
            // endregion

            // region Add default water/lava liquid block states
            val waterBlockState = Lod1LiquidBlockState(
                boundingBox = fullLodBoundingBox,
                density = 100.0f,
                dragCoefficient = 0.3f,
                fluidVel = Vector3f(),
                lod1LiquidBlockStateId = BlockTypeImpl.WATER.toInt(),
            )

            val lavaBlockState = Lod1LiquidBlockState(
                boundingBox = fullLodBoundingBox,
                density = 1000.0f,
                dragCoefficient = 1.0f,
                fluidVel = Vector3f(),
                lod1LiquidBlockStateId = BlockTypeImpl.LAVA.toInt(),
            )

            _liquidBlockStates.add(waterBlockState)
            _liquidBlockStates.add(lavaBlockState)
            _blockStateData.add(Triple(BlockTypeImpl.AIR.toInt(), BlockTypeImpl.WATER.toInt(), BlockTypeImpl.WATER.toInt()))
            _blockStateData.add(Triple(BlockTypeImpl.AIR.toInt(), BlockTypeImpl.LAVA.toInt(), BlockTypeImpl.LAVA.toInt()))
            // endregion
        }

        // A dummy world used to get the VoxelShape for each block state
        val dummyBlockGetter = object: BlockGetter {
            override fun getHeight(): Int = 255

            override fun getMinBuildHeight(): Int = 0

            override fun getBlockEntity(blockPos: BlockPos): BlockEntity? = null

            override fun getBlockState(blockPos: BlockPos): BlockState = Blocks.VOID_AIR.defaultBlockState()

            override fun getFluidState(blockPos: BlockPos): FluidState = Fluids.EMPTY.defaultFluidState()
        }

        // Create a map of common VoxelShape to Lod1SolidCollisionShape
        val voxelShapeToCollisionShapeMap = generateStairCollisionShapes(
            StairBlockAccessor.getTopShapes() + StairBlockAccessor.getBottomShapes() + SlabBlockAccessor.getBottomAABB() + SlabBlockAccessor.getTopAABB()
        )

        // Setup initial conditions for future ids
        var nextSolidId = 2
        var nextFluidId = 4
        var nextVoxelStateId = 4

        val generatedCollisionShapesMap = HashMap<VoxelShape, Lod1SolidCollisionShape?>()

        val fluidStateToBlockTypeMap = HashMap<FluidState, Pair<Lod1LiquidBlockStateId, BlockType>>()

        // Get the id of the fluid state/create a new fluid state if necessary
        fun getFluidState(fluidState: FluidState): Pair<Lod1LiquidBlockStateId, BlockType> {
            val cached = fluidStateToBlockTypeMap[fluidState]
            if (cached != null) return cached
            val maxY = ((fluidState.ownHeight * 16.0).roundToInt() - 1).coerceIn(0, 15).toByte()
            val fluidBox = LodBlockBoundingBox.createVSBoundingBox(0, 0, 0, 15, maxY, 15)
            val resourceLocation = BuiltInRegistries.FLUID.getKey(fluidState.type)
            if (fluidMap.containsKey(resourceLocation)) {
                val info = fluidMap[resourceLocation]
                val newFluidBlockState = Lod1LiquidBlockState(
                    boundingBox = fluidBox,
                    density = info?.density?.toFloat() ?: DEFAULT_DENSITY.toFloat(),
                    dragCoefficient = info?.dragCoefficient?.toFloat() ?: DEFAULT_DENSITY.toFloat(),
                    fluidVel = Vector3f(),
                    lod1LiquidBlockStateId = nextFluidId++,
                )
                val stateId = nextVoxelStateId++
                _liquidBlockStates.add(newFluidBlockState)
                _blockStateData.add(Triple(BlockTypeImpl.AIR.toInt(), newFluidBlockState.lod1LiquidBlockStateId, stateId))
                val blockTypeNew = BlockTypeImpl(stateId)
                fluidStateToBlockTypeMap[fluidState] = newFluidBlockState.lod1LiquidBlockStateId to blockTypeNew
                return newFluidBlockState.lod1LiquidBlockStateId to blockTypeNew
            } else {
                // Default
                return BlockTypeImpl.WATER.toInt() to BlockTypeImpl.WATER
            }
        }

        blockStates.forEach { blockState: BlockState ->
            val blockType: BlockType
            if (blockState.isAir) {
                blockType = vsCore.blockTypes.air
            } else {
                blockType = if (blockState.liquid()) {
                    getFluidState(blockState.fluidState).second
                } else if (blockState.isSolid) {
                    val voxelShape = blockState.getShape(dummyBlockGetter, BlockPos.ZERO)

                    val collisionShape: Lod1SolidCollisionShape = if (voxelShapeToCollisionShapeMap.contains(voxelShape)) {
                        voxelShapeToCollisionShapeMap[voxelShape]!!
                    } else if (generatedCollisionShapesMap.contains(voxelShape)) {
                        if (generatedCollisionShapesMap[voxelShape] != null) {
                            generatedCollisionShapesMap[voxelShape]!!
                        } else {
                            fullBlockCollisionShape
                        }
                    } else {
                        val generated = generateShapeFromVoxel(voxelShape)
                        generatedCollisionShapesMap[voxelShape] = generated
                        generated ?: fullBlockCollisionShape
                    }

                    val vsBlockStateInfo = map[BuiltInRegistries.BLOCK.getKey(blockState.block)]

                    // Create new solid block state
                    val solidStateId = nextSolidId++
                    val newSolidBlockState = Lod1SolidBlockState(
                        collisionShape = collisionShape,
                        elasticity = vsBlockStateInfo?.elasticity?.toFloat() ?: DEFAULT_ELASTICITY.toFloat(),
                        friction = vsBlockStateInfo?.friction?.toFloat() ?: DEFAULT_FRICTION.toFloat(),
                        hardness = DEFAULT_HARDNESS.toFloat(),
                        lod1SolidBlockStateId = solidStateId,
                    )
                    _solidBlockStates.add(newSolidBlockState)

                    // Create new voxel state
                    val blockStateId = nextVoxelStateId++

                    var fluidId = BlockTypeImpl.AIR.toInt()
                    if (!blockState.fluidState.isEmpty) {
                        fluidId = getFluidState(blockState.fluidState).first
                    }

                    _blockStateData.add(Triple(solidStateId, fluidId, blockStateId))
                    BlockTypeImpl(blockStateId)
                } else {
                    vsCore.blockTypes.air
                }
            }
            blockStateToId[blockState] = blockType
        }

        registeredBlocks = true
    }

    private val logger by logger()
}