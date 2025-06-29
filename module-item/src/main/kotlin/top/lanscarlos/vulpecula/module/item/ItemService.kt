package top.lanscarlos.vulpecula.module.item

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.common.core.utils.asLang
import kotlin.collections.plusAssign
import kotlin.jvm.optionals.getOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.item
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
object ItemService {

    fun build(type: String, amount: Int, durability: Int, name: String?, lore: List<String>?, shiny: Boolean, colored: Boolean, model: Int) : ItemStack {
        val material = XMaterial.matchXMaterial(type.uppercase()).getOrNull()
            ?: error(asLang("module-item-exception-invalid-material-type", type))
        val item = buildItem(material) {
            this.amount = amount
            this.damage = durability
            this.name = name
            if (!lore.isNullOrEmpty()) {
                this.lore += lore
            }
            if (shiny) {
                this.shiny()
            }
            if (colored) {
                this.colored()
            }
            if (model >= 0) {
                this.customModelData = model
            }
        }
        return item
    }

}