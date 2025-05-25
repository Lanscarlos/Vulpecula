package top.lanscarlos.vulpecula.module.schedule

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.message.errorSync
import top.lanscarlos.vulpecula.common.message.infoSync
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.schedule
 *
 * @author Lanscarlos
 * @since 2025/5/12 9:57
 */
object ScheduleService {

    private val directory: File = File(getDataFolder(), "schedule")

    private val registry = mutableMapOf<String, Schedule>()

    private val service: ConfigService = ConfigService(id = "schedule", directory = directory, priority = 8, callback = Callback)

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        // 自动注册配置服务
        Configs.register(service)
    }

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        // 自启动
        for ((_, schedule) in registry) {
            if (!schedule.isAutoStart) {
                return
            }
            schedule.start()
        }
    }

    fun get(id: String): Schedule {
        return getOrNull(id) ?: error("Schedule $id not found.")
    }

    fun getOrNull(id: String): Schedule? {
        return registry[id]
    }

    /**
     * 获取所有已注册的日程 ID
     * */
    fun keys(): Set<String> = registry.keys

    /**
     * 获取所有已注册的日程
     * */
    fun values(): Collection<Schedule> = registry.values

    /**
     * 获取所有已注册的日程键值对
     * */
    fun entries(): Set<Map.Entry<String, Schedule>> = registry.entries

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
            val schedule = when (val type = config.getString("type")?.lowercase()) {
                "periodic" -> PeriodicSchedule(id, config)
                "cron" -> CronSchedule(id, config)
                else -> error("Schedule $id type $type not supported.")
            }
            registry[id] = schedule
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            val schedule = (registry[id] as AbstractSchedule)
            schedule.stop("*")
            schedule.config.loadFromFile(file)
            if (schedule.isAutoStart) {
                schedule.start()
            }
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("schedule")
        }

        override fun onLoadCompleted(sender: ProxyCommandSender, time: Double) {
            sender.infoSync("module-schedule-service-load-success", registry.size, time)
        }

        override fun onLoadFailed(sender: ProxyCommandSender, id: String, file: File, e: Throwable) {
            e.printStackTrace()
            sender.errorSync("module-schedule-service-load-failure", id, e.localizedMessage)
        }
    }

}