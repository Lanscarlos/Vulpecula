package top.lanscarlos.vulpecula.module.item

import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.module.nms.MinecraftVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-12-14 15:58
 */
class ItemStackSource(val item: ItemStack) : ItemSource {

    override fun get(): ItemStack {
        return item
    }

    @Suppress("DEPRECATION")
    override fun set(item: ItemStack) {
        this.item.type = item.type
        this.item.amount = item.amount
        this.item.durability = item.durability
        this.item.itemMeta = item.itemMeta
    }

    override fun remove() {
        if (MinecraftVersion.major < MinecraftVersion.V1_12) {
            // 1.12 以下的版本可能存在问题
            warning("ItemStackSource#remove >> You are using a version lower than 1.12. Removing items may cause problems.")
        }
        item.amount = 0
    }

    override fun toStream(): ItemStream {
        return DefaultItemStream(this)
    }

}