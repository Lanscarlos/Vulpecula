package top.lanscarlos.vulpecula.module.dispatcher

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
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestCompileException
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author AI Assistant
 * @since 2025/6/6
 */
object DispatcherService {

    internal val name: String get() = Lang.DISPATCHER_DISPLAY_NAME.asText(console())

    private val directory: File = File(getDataFolder(), "dispatcher")

    private val registry = mutableMapOf<String, Dispatcher>()

    private val service: ConfigService = ConfigService("dispatcher", name, directory, 8, Callback)

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        // 自动注册配置服务
        Configs.register(service)
    }

    fun get(id: String): Dispatcher {
        return getOrNull(id) ?: error("Dispatcher $id not found.")
    }

    fun getOrNull(id: String): Dispatcher? {
        return registry[id]
    }

    /**
     * 获取所有已注册的调度器 ID
     * */
    fun keys(): Set<String> = registry.keys

    /**
     * 获取所有已注册的调度器
     * */
    fun values(): Collection<Dispatcher> = registry.values

    /**
     * 获取所有已注册的调度器键值对
     * */
    fun entries(): Set<Map.Entry<String, Dispatcher>> = registry.entries

    /**
     * 重载服务
     * */
    fun reload(sender: ProxyCommandSender) {
        service.load(sender)
    }

    private object Callback : ConfigServiceCallback {

        override fun onFileCreated(sender: ProxyCommandSender, id: String, file: File) {
            val config = Configuration.loadFromFile(file)
            val dispatcher = DefaultDispatcher(id, config)
            registry[id] = dispatcher
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            registry[id]!!.reload(file)
        }

        override fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File) {
            registry.remove(id)?.dispose()
        }

        override fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Throwable) {
            val cause = when (e) {
                is ConfigFieldReadException -> {
                    when (val cause = e.cause) {
                        is AbstractLocalizedException -> cause
                        else -> e
                    }
                }
                else -> e
            }
            val message = (cause as? AbstractLocalizedException)?.getLocalizedMessage(sender) ?: cause.localizedMessage
            if (message == null) {
                cause.printStackTrace()
            }
            Lang.DISPATCHER_CONFIG_LOAD_FAILURE.error(sender, id, message)
            if (cause is QuestCompileException) {
                cause.notice(sender)
            }
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("dispatcher")
        }

        override fun onLoadAutomatic(sender: ProxyCommandSender, id: String, file: File, time: Double) {
            Lang.DISPATCHER_CONFIG_LOAD_AUTOMATIC.info(sender, id, time)
        }

        override fun onLoadSuccess(sender: ProxyCommandSender, statistics: ConfigStatistics) {
            Lang.DISPATCHER_CONFIG_LOAD_SUCCESS.info(sender, registry.size, statistics.consumeTime)
        }

        override fun onLoadFailure(sender: ProxyCommandSender, e: Throwable) {
            // 加载器异常时需要清空已载入的对象
            registry.clear()
        }

    }
}