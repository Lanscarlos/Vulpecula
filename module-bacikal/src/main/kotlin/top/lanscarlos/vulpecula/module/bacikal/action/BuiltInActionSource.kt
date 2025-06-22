package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.pluginVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/6/18 17:23
 */
object BuiltInActionSource : ActionSource {

    override val name: String by lazy { pluginId }

    override val version: String by lazy { pluginVersion }

    override val authors: List<String> = listOf("Lanscarlos")

}