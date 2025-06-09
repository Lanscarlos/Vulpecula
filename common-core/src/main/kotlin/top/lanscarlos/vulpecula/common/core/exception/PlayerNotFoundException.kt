package top.lanscarlos.vulpecula.common.core.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.core.exception
 *
 * @author Lanscarlos
 * @since 2025/6/9 13:13
 */
class PlayerNotFoundException(val name: String) : RuntimeException() {

    override val message: String = asLang("common-core-exception-player-not-found", name)

}