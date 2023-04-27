package top.lanscarlos.vulpecula.bacikal.action.inventory

import top.lanscarlos.vulpecula.api.chemdah.InferItem.Companion.toInferItem

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.inventory
 *
 * @author Lanscarlos
 * @since 2023-04-12 09:13
 */
object ActionInventoryFind : ActionInventory.Resolver {

    override val name: Array<String> = arrayOf("find")

    override fun resolve(reader: ActionInventory.Reader): ActionInventory.Handler<out Any?> {
        return reader.handle {
            combine(
                source(),
                text("pattern"),
            ) { inventory, pattern ->
                val infer = pattern.toInferItem()
                for ((index, item) in inventory.withIndex()) {
                    if (infer.match(item)) return@combine index
                }
                return@combine -1
            }
        }
    }
}