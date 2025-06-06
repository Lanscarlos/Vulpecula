package top.lanscarlos.vulpecula.common.applicative

import top.lanscarlos.vulpecula.common.lang.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2025/5/29 10:50
 */
class UnsupportedTypeException(val source: Class<*>, val target: Class<*>) : RuntimeException() {

    override val message: String = asLang("common-applicative-exception-unsupported-type", source.name, target.name)

}