package top.lanscarlos.vulpecula.module.bacikal.parser

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

}