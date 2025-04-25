package top.lanscarlos.vulpecula.script

import taboolib.common.platform.ProxyCommandSender
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.script
 *
 * @author Lanscarlos
 * @since 2025-03-19 16:48
 */
interface Script {

    fun runActions(sender: ProxyCommandSender?, args: Map<String, Any>): CompletableFuture<*>

}