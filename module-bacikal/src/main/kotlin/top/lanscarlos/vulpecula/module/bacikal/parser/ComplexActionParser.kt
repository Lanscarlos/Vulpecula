package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * 复合语句解析器
 *
 * @author Lanscarlos
 * @since 2024-05-13 17:02
 */
class ComplexActionParser(
    id: String,
    name: String,
    aliases: Array<String>,
    namespace: String,
    description: String,
) : AbstractActionParser(id, name, aliases, namespace, description) {

    internal val actions: HashMap<String, BacikalActionParser> = hashMapOf()

    internal var defaultAction: BacikalActionParser? = null

    override fun buildVisualizedStructure(): ComponentText {
        return buildVisualizedStructure(" ")
    }

    fun buildVisualizedStructure(prefix: String): ComponentText {
        val indent = if (!id.contains('.')) {
            " ".repeat(2)
        } else {
            ""
        }
        val builder = Components.text(indent)
        builder += Components
            .text(name)
            .color(StandardColors.RED)
        builder.append(Components.text("").color(StandardColors.RESET))
        for ((index, action) in actions.values.withIndex()) {
            builder.newLine()
            builder.append(indent + prefix)
            if (index != actions.size - 1) {
                builder.append("├── ")
            } else {
                builder.append("└── ")
            }
            if (action is ComplexActionParser) {
                val tab = " ".repeat(4)
                val offset = " ".repeat((action.name.length - 4).coerceAtLeast(0))
                val newPrefix = if (index != actions.size - 1) {
                    "$indent$prefix│$tab$offset"
                } else {
                    "$indent$prefix $tab$offset"
                }
                builder.append(action.buildVisualizedStructure(newPrefix))
                builder.append(Components.text("").color(StandardColors.RESET))
            } else {
                builder.append(action.buildStructure())
            }
        }
        return builder
    }

    fun addActionParser(parser: BacikalActionParser) {
        val names = listOf(parser.name).plus(parser.aliases)
        for (name in names) {
            actions[name] = parser
        }
    }

    fun addDefaultParser(parser: BacikalActionParser) {
        defaultAction = parser
    }

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        reader.mark()
        val next = reader.nextToken()
        val parser = actions[next] ?: defaultAction ?: error("Unknown action '$next' at $id")
        return parser.resolve(reader)
    }

}