package top.lanscarlos.vulpecula.bacikal.quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-03 13:19
 */
abstract class BacikalException(override val cause: Throwable) : RuntimeException(cause)