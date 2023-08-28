package top.lanscarlos.vulpecula.core.bacikal.action.item

import org.bukkit.inventory.ItemStack
import top.lanscarlos.vulpecula.bacikal.parser.BacikalContext
import top.lanscarlos.vulpecula.bacikal.parser.BacikalFruit
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-08-29 11:11
 */
object ActionItemSwitch : ActionItem.Resolver {

    override val name = arrayOf("switch")

    override val resolver: BacikalContext.() -> BacikalFruit<*> = {
        fructus(
            any(),
            optional("from", then = text())
        ) { frame, source, slot ->
            when (source) {
                is ItemStack -> {
                    val getter = Supplier {  }
                }
            }
        }
    }
}