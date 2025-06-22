package top.lanscarlos.vulpecula.legacy.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-19 22:02
 */
annotation class BacikalParser(
    val id: String,
    val aliases: Array<String>,
    val namespace: String = "vulpecula",
    val shared: Boolean = true
)
