package top.lanscarlos.vulpecula.common.config.exception

import top.lanscarlos.vulpecula.common.core.utils.asLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config.exception
 *
 * @author Lanscarlos
 * @since 2025/6/7
 */
class UnsupportedFileExtensionException(val extension: String) : RuntimeException() {

    override val message: String = asLang("common-config-exception-unsupported-file-extension", extension)

}