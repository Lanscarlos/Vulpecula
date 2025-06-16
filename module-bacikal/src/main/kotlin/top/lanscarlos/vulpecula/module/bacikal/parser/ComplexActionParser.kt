package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestReader

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-05-13 17:02
 */
class ComplexActionParser(
    override val id: String,
    override val aliases: Array<String>,
    override val bind: String,
    override val namespace: String,
    override val description: String,
) : BacikalActionParser {

    internal val actions: HashMap<String, BacikalActionParser> = hashMapOf()

    internal var defaultAction: BacikalActionParser? = null

    fun registerAction(id: String, parser: BacikalActionParser) {
        actions[id] = parser
    }

    fun registerDefault(parser: BacikalActionParser) {
        defaultAction = parser
    }

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        reader.mark()
        val next = reader.nextToken()
        val parser = actions[next] ?: defaultAction ?: error("Unknown action '$next' at $id")
        return parser.resolve(reader)
    }

}