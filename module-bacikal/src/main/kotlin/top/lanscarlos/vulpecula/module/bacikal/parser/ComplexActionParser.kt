package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader

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