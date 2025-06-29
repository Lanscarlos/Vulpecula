package top.lanscarlos.vulpecula.module.item

import net.minecraft.world.level.entity.EntityInLevelCallback
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity
import org.bukkit.entity.Item
import taboolib.common.platform.function.submit
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.nmsProxy

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-11-28 16:59
 */
class DefaultVolatileAPI : VolatileAPI {

    override fun registerItemDespawnHandler(entity: Item) {
        val handle = (entity as CraftEntity).handle
        val levelCallback = handle.getProperty<EntityInLevelCallback?>("levelCallback", findToParent = true, remap = true)!!
        val proxy = nmsProxy<EntityInLevelCallback>("top.lanscarlos.vulpecula.module.item.DefaultEntityInLevelCallback", levelCallback, entity)
        submit {
            // 使用 submit 覆写才能生效
            handle.setLevelCallback(proxy)
        }
    }

}