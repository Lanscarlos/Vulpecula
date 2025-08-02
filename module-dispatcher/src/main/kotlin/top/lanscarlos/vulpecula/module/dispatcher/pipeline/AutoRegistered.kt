package top.lanscarlos.vulpecula.module.dispatcher.pipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12 9:26
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoRegistered(val value: String = "", val extends: String = "")
