package top.lanscarlos.vulpecula.common.config.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class ConfigFieldNotFoundException(val field: String) : RuntimeException() {

    override val message: String? = asLang("common-config-exception-field-not-found", field)

}