package top.lanscarlos.vulpecula.module.dispatcher

import taboolib.common.LifeCycle
import taboolib.common.TabooLib
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
import top.lanscarlos.vulpecula.common.utils.asLang
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

    internal val name: String get() = asLang("module-dispatcher-service-name")

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

            if (TabooLib.getCurrentLifeCycle() == LifeCycle.ACTIVE) {
                // 重载, 直接启用
                dispatcher.enable()
            }
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            registry[id]!!.reload(file)
        }

        override fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File) {
            registry.remove(id)?.dispose()
        }

        override fun onFileException(sender: ProxyCommandSender, id: String, file: File, e: Throwable) {
            sender.error(sync = true) { asLang("module-dispatcher-service-file-load-failure", id, e.localizedMessage) }
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
            releaseResourceFolder("dispatcher")
        }

        override fun onLoadAutomatic(sender: ProxyCommandSender, id: String, file: File, time: Double) {
            sender.info(sync = true) { asLang("module-dispatcher-service-load-automatic", id, time) }
        }

        override fun onLoadSuccess(sender: ProxyCommandSender, created: Int, modified: Int, deleted: Int, failed: Int, time: Double) {
            if (created > 0) {
                sender.info(sync = true) { asLang("module-dispatcher-service-load-detail-created", created) }
            }
            if (modified > 0) {
                sender.info(sync = true) { asLang("module-dispatcher-service-load-detail-modified", modified) }
            }
            if (deleted > 0) {
                sender.info(sync = true) { asLang("module-dispatcher-service-load-detail-deleted", deleted) }
            }
            if (failed > 0) {
                sender.warning(sync = true) { asLang("module-dispatcher-service-load-detail-failed", failed) }
            }
            sender.info(sync = true) { asLang("module-dispatcher-service-load-success", registry.size, time) }
        }

        override fun onLoadFailure(sender: ProxyCommandSender, time: Double, e: Throwable) {
            e.printStackTrace()
            sender.error(sync = true) { asLang("module-dispatcher-service-load-failure", e.localizedMessage) }
        }

    }
}