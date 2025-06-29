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
class ExceptionalActionParser(val exception: Exception) : BacikalActionParser {

    override val id: String get() = throw exception

    override val name: String get() = throw exception

    override val aliases: Array<String> get() = throw exception

    override val namespace: String get() = throw exception

    override val description: String get() = throw exception

    override val source: ActionSource get() = throw exception

    override fun buildStructure(depth: Int): ComponentText = throw exception

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> = throw exception

}