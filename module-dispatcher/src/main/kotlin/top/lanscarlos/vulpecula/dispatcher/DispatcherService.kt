package top.lanscarlos.vulpecula.module.dispatcher

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
import top.lanscarlos.vulpecula.common.lang.asLang
import top.lanscarlos.vulpecula.common.lang.error
import top.lanscarlos.vulpecula.common.lang.info
import top.lanscarlos.vulpecula.dispatcher.DefaultDispatcher
import top.lanscarlos.vulpecula.dispatcher.Dispatcher
import top.lanscarlos.vulpecula.module.bacikal.exception.BacikalCompileException
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher
 *
 * @author AI Assistant
 * @since 2025/6/6
 */
object DispatcherService {

    private val directory: File = File(getDataFolder(), "dispatcher")

    private val registry = mutableMapOf<String, Dispatcher>()

    private val service: ConfigService = ConfigService(id = "dispatcher", directory = directory, priority = 8, callback = Callback)

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

        override fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File) {
            registry.remove(id)
        }

        override fun onFileCreated(sender: ProxyCommandSender, id: String, file: File) {
            val config = Configuration.loadFromFile(file)
            val dispatcher = DefaultDispatcher(id, config)
            registry[id] = dispatcher
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            val dispatcher = (registry[id] as DefaultDispatcher)
            dispatcher.config.loadFromFile(file)
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("dispatcher")
        }

        override fun onLoadCompleted(sender: ProxyCommandSender, time: Double) {
            sender.info(sync = true) { asLang("module-dispatcher-service-load-success", registry.size, time) }
        }

        override fun onLoadFailed(sender: ProxyCommandSender, id: String, file: File, e: Throwable) {
            sender.error(sync = true) { asLang("module-dispatcher-service-load-failure", id, e.localizedMessage) }
            when (e) {
                is ConfigFieldNotFoundException -> {}
                is ConfigFieldReadException -> {
                    when (val cause = e.cause) {
                        is BacikalCompileException -> cause.printLocalizedMessage(sender)
                    }
                }
                else -> {
                    e.printStackTrace()
                }
            }
        }
    }
}