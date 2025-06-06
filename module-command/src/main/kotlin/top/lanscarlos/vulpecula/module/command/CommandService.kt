package top.lanscarlos.vulpecula.module.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.common5.util.getStackTraceString
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.lang.*
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 10:47
 */
object CommandService {

    private val directory: File = File(getDataFolder(), "command")

    private val service: ConfigService = ConfigService(id = "command", directory = directory, priority = 8, callback = Callback)

    private val commands: HashMap<String, CustomCommand> = hashMapOf()

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        // 自动注册配置服务
        Configs.register(service)
    }

//    @Awake(LifeCycle.ACTIVE)
//    private fun onActive() {
//        // 注册命令
//        for ((_, command) in commands) {
//            command.register()
//        }
//    }

    /**
     * 重载服务
     * */
    fun reload(sender: ProxyCommandSender) {
        service.load(sender)
    }

    private object Callback : ConfigServiceCallback {

        override fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File) {
            commands.remove(id)?.unregister()
        }

        override fun onFileCreated(sender: ProxyCommandSender, id: String, file: File) {
            val command = CustomCommand(id, Configuration.loadFromFile(file))
            command.register()
            commands[id] = command
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            val command = commands[id]!!
            command.config.loadFromFile(file)
            command.rebuild()
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("command")
        }

        override fun onLoadCompleted(sender: ProxyCommandSender, time: Double) {
            sender.info(sync = true) { asLang("module-command-service-load-succeeded", commands.size, time) }
        }

        override fun onLoadFailed(sender: ProxyCommandSender, id: String, file: File, e: Throwable) {
            sender.error(sync = true) { asLang("module-command-service-load-failed", id, e.localizedMessage, e.getStackTraceString()) }
            sender.error(sync = true) { e.localizedMessage }
        }

    }

}