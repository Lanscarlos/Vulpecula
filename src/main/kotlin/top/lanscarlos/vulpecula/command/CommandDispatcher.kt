package top.lanscarlos.vulpecula.command

import org.bukkit.command.CommandSender
import taboolib.common.platform.command.component.CommandComponent
import top.lanscarlos.vulpecula.internal.EventDispatcher
import top.lanscarlos.vulpecula.utils.sendSyncLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.command
 *
 * @author Lanscarlos
 * @since 2023-02-08 21:29
 */
object CommandDispatcher {

    val main: CommandComponent.() -> Unit = {
        literal("enable", literal = enable)
        literal("disable", literal = disable)
        literal("list", literal = list)
        literal("detail", literal = detail)
    }

    val enable: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                EventDispatcher.cache.values.mapNotNull {
                    if (it.isStopped) it.id else null
                }.plus("*")
            }
            execute<CommandSender> { sender, _, id ->
                if (id == "*") {
                    // 启动所有调度模块
                    EventDispatcher.cache.values.forEach { it.registerListener() }
                    sender.sendSyncLang("Dispatcher-Enable-All-Succeeded", id)
                } else {
                    EventDispatcher.get(id)?.registerListener()?.let {
                        sender.sendSyncLang("Dispatcher-Enable-Succeeded", id)
                    } ?: sender.sendSyncLang("Dispatcher-Not-Found", id)
                }
            }
        }
    }

    val disable: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                EventDispatcher.cache.values.mapNotNull {
                    if (it.isRunning) it.id else null
                }.plus("*")
            }
            execute<CommandSender> { sender, _, id ->
                if (id == "*") {
                    // 关闭所有调度模块
                    EventDispatcher.cache.values.forEach { it.unregisterListener() }
                    sender.sendSyncLang("Dispatcher-Disable-All-Succeeded", id)
                } else {
                    EventDispatcher.get(id)?.unregisterListener()?.let {
                        sender.sendSyncLang("Dispatcher-Disable-Succeeded", id)
                    } ?: sender.sendSyncLang("Dispatcher-Not-Found", id)
                }
            }
        }
    }

    val list: CommandComponent.() -> Unit = {
        execute<CommandSender> { sender, _, _ ->
            sender.sendSyncLang(
                "Dispatcher-List",
                EventDispatcher.cache.values.joinToString(", ") { it.id },
                EventDispatcher.cache.values.filter { it.isRunning }.joinToString(", ") { it.id },
            )
        }
    }

    val detail: CommandComponent.() -> Unit = {
        dynamic("id") {
            suggestion<CommandSender>(uncheck = true) { _, _ ->
                EventDispatcher.cache.keys.toList()
            }
            execute<CommandSender> { sender, _, id ->
                EventDispatcher.get(id)?.let { dispatcher ->
                    sender.sendSyncLang(
                        "Dispatcher-Detail",
                        id,
                        if (dispatcher.isRunning) "§aRUNNING" else "§cSTOPPED",
                        dispatcher.eventName, dispatcher.priority.name, dispatcher.ignoreCancelled,
                        dispatcher.handlers.joinToString(", ") { it.id }
                    )
                } ?: sender.sendSyncLang("Dispatcher-Not-Found", id)
            }
        }
    }

}