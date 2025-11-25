package top.lanscarlos.vulpecula.module.command

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import top.lanscarlos.vulpecula.common.config.ConfigStatistics
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldNotFoundException
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestCompileException
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.command
 *
 * @author Lanscarlos
 * @since 2025/4/30 10:47
 */
object CommandService {

    internal val name: String get() = Lang.MODULE_COMMAND_DISPLAY_NAME.asText(console())

    private val directory: File = File(getDataFolder(), "command")

    private val service: ConfigService = ConfigService("command", name, directory, 8, Callback)

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

        override fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File) {
            registry.remove(id)?.unregister()
        }

        override fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Throwable) {
            Lang.MODULE_COMMAND_LOAD_FAILURE.error(sender, id, e.localizedMessage ?: "")
            when (e) {
                is ConfigFieldNotFoundException -> {}
                is ConfigFieldReadException -> {
                    when (val cause = e.cause) {
                        is QuestCompileException -> cause.notice(sender)
                    }
                }
                is QuestCompileException -> e.notice(sender)
                else -> e.printStackTrace()
            }
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("command")
        }

        override fun onLoadAutomatic(sender: ProxyCommandSender, id: String, file: File, time: Double) {
            Lang.MODULE_COMMAND_LOAD_AUTOMATIC.info(sender, id, time)
        }

        override fun onLoadSuccess(sender: ProxyCommandSender, statistics: ConfigStatistics) {
            Lang.MODULE_COMMAND_LOAD_SUCCESS.info(sender, registry.size, statistics.consumeTime)
        }

        override fun onLoadFailure(sender: ProxyCommandSender, e: Throwable) {
            // 加载器异常时需要清空已载入的对象
            registry.clear()
        }

    }

}