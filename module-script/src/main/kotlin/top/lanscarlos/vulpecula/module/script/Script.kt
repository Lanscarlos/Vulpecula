package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-19 16:48
 */
interface Script {

    val id: String

    val file: File

    fun execute(sender: ProxyCommandSender?, args: Map<String, Any>): ScriptTask

}