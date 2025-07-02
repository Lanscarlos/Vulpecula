package top.lanscarlos.vulpecula.module.bacikal.action

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * 语句来源
 *
 * @author Lanscarlos
 * @since 2025/6/18 17:11
 */
interface ActionSource {

    val name: String

    val version: String

    val authors: List<String>

    fun getActionMetadata(name: String): Array<String>

}