package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.info
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import kotlin.jvm.optionals.getOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.item
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
@BacikalParser("item.build")
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
        @Additional(["model"]) model: Int = -1,
    ) : ItemStack {
        info("Action item.build >> type=$type; amount=$amount; durability=$durability; name=$name; lore=$lore; shiny=$shiny; model=$model;")
        val material = XMaterial.matchXMaterial(type.uppercase()).getOrNull() ?: error("Unknown material type: $type")
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
        frame.setVariable(ActionItem.CONTEXT, item)
        return item
    }

}