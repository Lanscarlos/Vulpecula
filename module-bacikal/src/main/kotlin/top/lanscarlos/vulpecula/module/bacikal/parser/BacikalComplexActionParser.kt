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
class BacikalComplexActionParser(val name: String) : QuestActionParser {

    val actions = linkedMapOf<String, BacikalActionParser>()

    fun registerAction(id: String, parser: BacikalActionParser) {
        actions[id] = parser
    }

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        reader.mark()
        val next = reader.nextToken()
        val parser = actions[next] ?: actions["@DEFAULT"] ?: error("Unknown action '$next' at $name")
        return parser.resolve(reader)
    }

}