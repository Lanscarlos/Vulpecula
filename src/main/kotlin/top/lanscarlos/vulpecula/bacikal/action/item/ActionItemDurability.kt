package top.lanscarlos.vulpecula.bacikal.action.item

import top.lanscarlos.vulpecula.utils.dura
import top.lanscarlos.vulpecula.utils.maxDurability

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-03-24 16:32
 */
object ActionItemDurability : ActionItem.Resolver {

    override val name: Array<String> = arrayOf("durability", "dura")

    override fun resolve(reader: ActionItem.Reader): ActionItem.Handler<out Any?> {
        val source = reader.source().accept(reader)

        reader.mark()
        return when (reader.nextToken()) {
            "add", "fix" -> {
                reader.transfer {
                    combine(
                        source,
                        trim("to", then = int(0))
                    ) { item, dura ->
                        item.also { it.dura += dura }
                    }
                }
            }
            "sub", "take" -> {
                reader.transfer {
                    combine(
                        source,
                        trim("to", then = int(0))
                    ) { item, dura ->
                        item.also { it.dura -= dura }
                    }
                }
            }
            "set", "modify" -> {
                reader.transfer {
                    combine(
                        source,
                        trim("to", then = intOrNull())
                    ) { item, dura ->
                        item.also { it.dura = dura ?: it.dura }
                    }
                }
            }
            "current", "cur" -> {
                reader.handle {
                    combine(source) { item -> item.dura }
                }
            }
            "max" -> {
                reader.handle {
                    combine(source) { item -> item.maxDurability }
                }
            }
            else -> {
                reader.reset()
                // 默认返回耐久度
                reader.handle {
                    combine(source) { item -> item.dura }
                }
            }
        }
    }
}