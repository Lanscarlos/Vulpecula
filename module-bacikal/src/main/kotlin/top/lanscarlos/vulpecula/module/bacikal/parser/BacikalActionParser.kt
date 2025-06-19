package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.library.kether.QuestActionParser
import taboolib.module.chat.ComponentText

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

    /**
     * 构建可视化结构用于展示结构
     *
     * @param depth 层级, -1 为展开所有层级
     * */
    fun buildStructure(depth: Int): ComponentText

}