package io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.chassis.sticker

import io.github.xiewuzhiying.vs_addition.stuff.constraint.ConstraintGroup
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import org.valkyrienskies.physics_api.ConstraintId

open class StickerConstraintGroup(override val constraintId0: ConstraintId, override val constraintId1: ConstraintId, open val blockPos: BlockPos) : ConstraintGroup(constraintId0, constraintId1) {
    constructor(tag: CompoundTag) : this(tag.getInt("constraintId0"), tag.getInt("constraintId1"), BlockPos(tag.getInt("blockPosX"), tag.getInt("blockPosY"), tag.getInt("blockPosZ")))
    override val compoundTag : CompoundTag
        get() {
            val tag = super.compoundTag
            tag.putInt("blockPosX", blockPos.x)
            tag.putInt("blockPosY", blockPos.y)
            tag.putInt("blockPosZ", blockPos.z)
            return tag
        }
}