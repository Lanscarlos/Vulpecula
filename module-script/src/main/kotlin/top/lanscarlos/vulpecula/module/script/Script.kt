package top.lanscarlos.vulpecula.module.script

import taboolib.common.platform.ProxyCommandSender

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025-03-19 16:48
 */
interface Script {

    val id: String

    fun run(
        sender: ProxyCommandSender?,
        args: List<Any?>,
        variables: Map<String, Any>
    ): ScriptTask

}