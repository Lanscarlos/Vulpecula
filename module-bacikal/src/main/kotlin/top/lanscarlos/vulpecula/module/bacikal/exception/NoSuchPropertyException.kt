package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.lang.Lang
import java.lang.RuntimeException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025/8/3
 */
class NoSuchPropertyException(val clazz: Class<*>, val property: String) : RuntimeException() {

    override val message: String = Lang.BACIKAL_NO_SUCH_EXCEPTION.asText(console(), clazz.name, property)

}