package top.lanscarlos.vulpecula.common.exception

import top.lanscarlos.vulpecula.common.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/9 13:16
 */
class WorldNotFoundException(val name: String) : RuntimeException() {

    override val message: String = asLang("common-core-exception-world-not-found", name)

}