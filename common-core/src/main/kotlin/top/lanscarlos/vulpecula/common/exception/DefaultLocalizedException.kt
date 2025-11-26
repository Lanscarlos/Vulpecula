package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.exception
 *
 * @author Lanscarlos
 * @since 2025/11/26
 */
abstract class DefaultLocalizedException(override val lang: Lang, vararg arguments: Any) : AbstractLocalizedException() {

    override val arguments: Array<Any> = arguments.toList().toTypedArray()

}