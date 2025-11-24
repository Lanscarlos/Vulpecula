package top.lanscarlos.vulpecula.common.applicative.exception

import top.lanscarlos.vulpecula.common.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative.exception
 *
 * @author Lanscarlos
 * @since 2025/5/29 10:50
 */
class TypeConversionException(val source: Any, val targetClass: Class<*>) : RuntimeException() {

    val sourceClass: Class<*> = source.javaClass

    override val message: String = asLang("common-applicative-exception-unsupported-type", sourceClass.name, targetClass.name)

}