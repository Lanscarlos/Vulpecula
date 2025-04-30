package top.lanscarlos.vulpecula.module.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
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

    private val directory: File = File(getDataFolder(), "script")

    private val service: ConfigService = ConfigService(id = "script", directory = directory, priority = 8, callback = Callback)

    private val commands: HashMap<String, CustomCommand> = hashMapOf()

    init {
        Configs.register(service)
    }

    @Awake(LifeCycle.ACTIVE)
    private fun onActive() {
        // 注册命令
        for ((_, command) in commands) {
            command.register()
        }
    }

    /**
     * 重载服务
     * */
    fun reload(): String {
        return service.load()
    }

    private object Callback : ConfigServiceCallback {

        override fun onFileDeleted(id: String, file: File) {
            commands.remove(id)
        }

        override fun onFileCreated(id: String, file: File) {
            val command = CustomCommand(id, Configuration.loadFromFile(file))
            commands[id] = command
        }

        override fun onFileModified(id: String, file: File) {
            val command = commands[id]!!
            command.rebuild()
        }

        override fun onLoadInit(directory: File) {
            releaseResourceFolder("command")
        }

        override fun onLoadCompleted(time: Double): String {
            return console().asLangText("module-command-service-load-succeeded", commands.size, time)
        }

        override fun onLoadFailed(e: Throwable): String {
            return console().asLangText("module-command-service-load-failed", e.localizedMessage)
        }

    }

}