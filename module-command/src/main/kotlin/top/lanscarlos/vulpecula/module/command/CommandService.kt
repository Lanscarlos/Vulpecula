package top.lanscarlos.vulpecula.module.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldNotFoundException
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.common.core.utils.debug
import top.lanscarlos.vulpecula.common.core.utils.error
import top.lanscarlos.vulpecula.common.core.utils.info
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalCompileException
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 10:47
 */
object CommandService {

    internal val module: String by lazy { asLang("module-command-service-name") }

    private val directory: File = File(getDataFolder(), "command")

    private val service: ConfigService = ConfigService(id = "command", directory = directory, priority = 8, callback = Callback)

    private val registry: HashMap<String, CustomCommand> = hashMapOf()

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
            registry.remove(id)?.unregister()
        }

        override fun onFileCreated(sender: ProxyCommandSender, id: String, file: File) {
            val command = CustomCommand(id, Configuration.loadFromFile(file))
            command.register()
            registry[id] = command
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            val command = registry[id]!!
            command.config.loadFromFile(file)
            command.rebuild()
        }

        override fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Exception) {
            sender.error(module, sync = true) { asLang("module-command-service-load-failure", id, e.localizedMessage) }
            when (e) {
                is ConfigFieldNotFoundException -> {}
                is ConfigFieldReadException -> {
                    when (val cause = e.cause) {
                        is BacikalCompileException -> cause.printLocalizedMessage(sender, module)
                    }
                }
                else -> {
                    e.printStackTrace()
                }
            }
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("command")
        }

        override fun onLoadSuccess(sender: ProxyCommandSender, detail: String, time: Double) {
            sender.debug(module, sync = true) { asLang("module-command-service-load-detail", detail)}
            sender.info(module, sync = true) { asLang("module-command-service-load-success", registry.size, time) }
        }

        override fun onLoadFailure(sender: ProxyCommandSender, time: Double, e: Throwable) {
            e.printStackTrace()
            sender.error(module, sync = true) { asLang("module-command-service-load-failure", e.localizedMessage) }
        }


    }

}