package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.console
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import kotlin.collections.plusAssign
import kotlin.jvm.optionals.getOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
@Parser("item.build")
object ActionItemBuild : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        type: String,
        @Additional(["amount", "amt"]) amount: Int = 1,
        @Additional(["durability", "dura"]) durability: Int = 0,
        @Additional(["name"]) name: String?,
        @Additional(["lore"]) lore: List<String>?,
        @Additional(["shiny"]) shiny: Boolean = false,
        @Additional(["colored"]) colored: Boolean = true,
        @Additional(["model"]) model: Int = -1
    ) : ItemStack {
        val material = XMaterial.matchXMaterial(type.uppercase()).getOrNull()
            ?: error(Lang.ACTION_ITEM_EXCEPTION_INVALID_MATERIAL_TYPE.asText(console(), type))
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
        ActionItem.setContext(frame, item)
        return item
    }

}