package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestReader
import taboolib.module.chat.ComponentText
import top.lanscarlos.vulpecula.module.bacikal.action.ActionSource

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/29
 */
class ExceptionalActionParser(
    override val id: String,
    override val name: String,
    override val aliases: Array<String>,
    override val namespace: String,
    override val description: String,
    override val source: ActionSource,
    val exception: Exception
) : BacikalActionParser {

    override fun buildStructure(depth: Int): ComponentText {
        throw exception
    }

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T>? {
        throw exception
    }

}