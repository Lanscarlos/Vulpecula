package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestActionParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/16
 */
interface BacikalActionParser : QuestActionParser {

    val id: String

    val name: String

    val aliases: Array<String>

    val namespace: String

    val description: String

}