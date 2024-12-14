package top.lanscarlos.vulpecula.item

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.EntityInLevelCallback
import org.bukkit.entity.Item
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-11-28 17:40
 */
class DefaultEntityInLevelCallback(private val source: EntityInLevelCallback, val entity: Item) : EntityInLevelCallback {

    init {
        info("DefaultEntityInLevelCallback >> proxy created.")
    }

    fun a() {
        source.onMove()
    }

    fun a(reason: Entity.RemovalReason?) {
        source.onRemove(reason)
        info("DefaultEntityInLevelCallback >> gaga onRemove by ${reason?.name}")
    }

    override fun onMove() {
        warning("DefaultEntityInLevelCallback >> onMove")
    }

    override fun onRemove(p0: Entity.RemovalReason?) {
        warning("DefaultEntityInLevelCallback >> onRemove")
    }

}