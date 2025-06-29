package top.lanscarlos.vulpecula.module.action.item

import org.bukkit.inventory.ItemStack
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import top.lanscarlos.vulpecula.module.item.ItemService

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
        val item = ItemService.build(type, amount, durability, name, lore, shiny, colored, model)
        frame.setVariable(ActionItem.CONTEXT, item)
        return item
    }

}