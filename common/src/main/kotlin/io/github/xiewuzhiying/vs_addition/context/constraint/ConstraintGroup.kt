package io.github.xiewuzhiying.vs_addition.context.constraint

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.valkyrienskies.physics_api.ConstraintId

open class ConstraintGroup(open val constraintIds: Iterable<ConstraintId>) {
    open val compoundTag : CompoundTag
        get() {
            val tag = CompoundTag()
            val list = ListTag()
            constraintIds.forEach {
                val tmp = CompoundTag()
                tmp.putInt("constraintId", it)
                list.add(tmp)
            }
            tag.put("constraintGroup", list)
            return tag
        }

    companion object {
        @JvmStatic
        fun createFromTag(tag: CompoundTag): ConstraintGroup {
            return ConstraintGroup(getConstraintsFromTag(tag))
        }

        @JvmStatic
        fun getConstraintsFromTag(tag: CompoundTag): Iterable<ConstraintId> {
            return (tag.get("constraintGroup") as ListTag).map { (it as CompoundTag).getInt("constraintId") }
        }
    }
}