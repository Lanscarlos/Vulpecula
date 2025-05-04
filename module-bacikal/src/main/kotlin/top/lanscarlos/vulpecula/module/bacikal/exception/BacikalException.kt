package top.lanscarlos.vulpecula.module.bacikal.exception

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025-05-03 13:19
 */
abstract class BacikalException(override val cause: Throwable) : RuntimeException(cause)