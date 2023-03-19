package top.lanscarlos.vulpecula.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-19 22:02
 */
annotation class BacikalParser(
    val id: String,
    val name: Array<String>,
    val namespace: String = "vulpecula",
    val shared: Boolean = true,
    val override: Array<String> = []
)
