package top.lanscarlos.vulpecula.bacikal.action.item

import top.lanscarlos.vulpecula.utils.chemdah.InferItem.Companion.toInferItem

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-04-04 22:45
 */
object ActionItemMatch : ActionItem.Resolver {

    override val name: Array<String> = arrayOf("match")

    /**
     * item match &item by &pattern
     * */
    override fun resolve(reader: ActionItem.Reader): ActionItem.Handler<out Any?> {
        return reader.handle {
            combine(
                source(),
                text(display = "pattern")
            ) { item, pattern ->
                pattern.toInferItem().match(item)
            }
        }
    }
}