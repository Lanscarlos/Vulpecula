package top.lanscarlos.vulpecula.module.bacikal.annotation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.annotation
 *
 * @author Lanscarlos
 * @since 2024-11-20 17:40
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BacikalParser(
    val id: String,
    val name: String = "",
    val aliases: Array<String> = [],
    val namespace: String = "vulpecula",
    val description: String = ""
)
