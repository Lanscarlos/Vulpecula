package top.lanscarlos.vulpecula.kether

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-18 18:04
 */
annotation class VulKetherParser(
    val id: String,
    val name: Array<String>,
    val namespace: String = "vulpecula",
    val shared: Boolean = true,
    val override: Array<String> = []
)
