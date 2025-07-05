package top.lanscarlos.vulpecula.module.bacikal.annotation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.annotation
 *
 * @author Lanscarlos
 * @since 2024-11-20 11:06
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Expected(val values: Array<String>)
