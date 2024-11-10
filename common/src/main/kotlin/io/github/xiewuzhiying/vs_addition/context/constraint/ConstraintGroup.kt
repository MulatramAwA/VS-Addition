package io.github.xiewuzhiying.vs_addition.context.constraint

import net.minecraft.nbt.CompoundTag
import org.valkyrienskies.physics_api.ConstraintId

open class ConstraintGroup(open val constraintId0: ConstraintId, open val constraintId1: ConstraintId) {
    constructor(tag: CompoundTag) : this(tag.getInt("constraintId0"), tag.getInt("constraintId1"))
    open val compoundTag : CompoundTag
        get() {
            val tag = CompoundTag()
            tag.putInt("constraintId0", constraintId0)
            tag.putInt("constraintId1", constraintId1)
            return tag
        }
}