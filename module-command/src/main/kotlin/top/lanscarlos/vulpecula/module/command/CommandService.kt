package top.lanscarlos.vulpecula.module.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.common.config.ConfigLoadContext
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import top.lanscarlos.vulpecula.common.config.Configs
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
    fun onEnable() {
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
    fun reload(): ConfigLoadContext {
        val context = ConfigLoadContext()
        service.load(context)
        return context
    }

    private object Callback : ConfigServiceCallback {

        override fun onFileDeleted(context: ConfigLoadContext, id: String, file: File) {
            commands.remove(id)?.unregister()
        }

        override fun onFileCreated(context: ConfigLoadContext, id: String, file: File) {
            val command = CustomCommand(id, Configuration.loadFromFile(file))
            command.register()
            commands[id] = command
        }

        override fun onFileModified(context: ConfigLoadContext, id: String, file: File) {
            val command = commands[id]!!
            command.config.loadFromFile(file)
            command.rebuild()
        }

        override fun onLoadInit(context: ConfigLoadContext, directory: File) {
            releaseResourceFolder("command")
        }

        override fun onLoadCompleted(context: ConfigLoadContext, time: Double) {
            context.logs += console().asLangText("module-command-service-load-succeeded", commands.size, time)
        }

        override fun onLoadFailed(context: ConfigLoadContext, id: String, file: File, e: Throwable) {
            e.printStackTrace()
            context.logs += console().asLangText("module-command-service-load-failed", e.localizedMessage)
        }

    }

}