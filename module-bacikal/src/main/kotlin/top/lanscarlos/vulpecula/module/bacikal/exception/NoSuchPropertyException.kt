package top.lanscarlos.vulpecula.module.bacikal.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang
import java.lang.RuntimeException

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025/8/3
 */
class NoSuchPropertyException(val clazz: Class<*>, val property: String) : RuntimeException() {

    override val message: String = asLang("module-bacikal-exception-no-such-exception", clazz.name, property)

}