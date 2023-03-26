package top.lanscarlos.vulpecula.bacikal.action.inventory

import top.lanscarlos.vulpecula.utils.chemdah.InferItem.Companion.toInferItem

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.inventory
 *
 * @author Lanscarlos
 * @since 2023-03-26 14:40
 */
object ActionInventoryTake : ActionInventory.Resolver {

    override val name: Array<String> = arrayOf("take")

    override fun resolve(reader: ActionInventory.Reader): ActionInventory.Handler<out Any?> {
        return reader.handle {
            combine(
                source(),
                text("pattern"),
                optional("amount", "amt", then = int(display = "amount"), def = 1)
            ) { inventory, pattern, amount ->
                pattern.toInferItem().take(inventory, amount)
            }
        }
    }
}