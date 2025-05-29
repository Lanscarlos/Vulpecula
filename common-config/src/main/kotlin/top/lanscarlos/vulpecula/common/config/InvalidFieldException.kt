package top.lanscarlos.vulpecula.common.config

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025/5/29 9:36
 */
class InvalidFieldException(val field: String, override val cause: Throwable) : RuntimeException()