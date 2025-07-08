package top.lanscarlos.vulpecula.module.action.illusion

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.illusion
 *
 * @author Lanscarlos
 * @since 2025/7/8 14:20
 */
interface WarningEffect {

    val level: Int

    fun apply()

    fun stop()

}