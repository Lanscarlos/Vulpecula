package top.lanscarlos.vulpecula.module.bacikal.extension

import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.pluginVersion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.extension
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */
object NativeExtension : Extension {

    override val name: String by lazy { pluginId }

    override val version: String by lazy { pluginVersion }

    override val authors: List<String> = listOf("Lanscarlos")

}