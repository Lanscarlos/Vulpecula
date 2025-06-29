package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.StandardColors

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/17
 */
abstract class AbstractActionParser(
    override val id: String,
    name: String,
    override val aliases: Array<String>,
    override val namespace: String,
    override val description: String,
) : BacikalActionParser {

    override val name: String = name.ifBlank { id.substringAfterLast('.') }

    abstract fun onDrawStructure(maxDepth: Int, currentDepth: Int): List<ComponentText>

    protected fun ComponentText.resetColor(): ComponentText {
        return append(Components.text("&7"))
    }

}