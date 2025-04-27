package top.lanscarlos.vulpecula.module.script

import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/4/25 10:00
 */
object ScriptService {

    private val directory: File = File(getDataFolder(), "script")

    private val scripts: HashMap<String, Script> = hashMapOf()

    private val tasks: HashMap<Long, ScriptTask> = hashMapOf()

    private var pid: Long = 0

    private val service: ConfigService = ConfigService(id = "script", directory = directory, priority = 8, callback = Callback)

    init {
        // 注册配置服务
        Configs.register(service)
    }

    /**
     * 获取脚本
     *
     * @param id 脚本 ID
     * @throws IllegalStateException 脚本不存在
     * @return 脚本
     * */
    fun get(id: String): Script = getOrNull(id) ?: error("Script not found: $id")

    /**
     * 获取脚本
     *
     * @param id 脚本 ID
     * @return 脚本, 或 null
     * */
    fun getOrNull(id: String): Script? = scripts[id]

    /**
     * 获取所有已注册的脚本 ID
     * */
    fun keys(): Set<String> = scripts.keys

    /**
     * 获取所有已注册的脚本
     * */
    fun values(): Collection<Script> = scripts.values

    /**
     * 获取所有已注册的脚本键值对
     * */
    fun entries(): Set<Map.Entry<String, Script>> = scripts.entries

    /**
     * 获取正在运行的任务
     * @throws IllegalStateException 脚本不存在
     * */
    fun getTask(pid: Long): ScriptTask = getTaskOrNull(pid) ?: error("Task not found: $pid")

    /**
     * 获取正在运行的任务
     * */
    fun getTaskOrNull(pid: Long): ScriptTask? = tasks[pid]

    /**
     * 获取所有正在运行的脚本 ID
     * */
    fun getTaskKeys(): Set<Long> = tasks.keys

    /**
     * 获取所有正在运行的脚本
     * */
    fun getTaskValues(): Collection<ScriptTask> = tasks.values

    /**
     * 获取所有正在运行的脚本键值对
     * */
    fun getTaskEntries(): Set<Map.Entry<Long, ScriptTask>> = tasks.entries

    /**
     * 运行指定脚本
     *
     * @param id 脚本 ID
     * @param player 玩家
     * @param args 脚本参数
     * @throws IllegalStateException 脚本不存在
     * @return 运行结果
     * */
    fun run(id: String, player: Player, args: Map<String, Any>): CompletableFuture<*> {
        return run(id, adaptPlayer(player), args)
    }

    /**
     * 运行指定脚本
     *
     * @param id 脚本 ID
     * @param sender 脚本执行者
     * @param args 脚本参数
     * @throws IllegalStateException 脚本不存在
     * @return 运行结果
     * */
    fun run(id: String, sender: ProxyCommandSender?, args: Map<String, Any>): CompletableFuture<*> {
        val task = get(id).execute(sender, args)
        if (task.isDone) {
            return task.future
        }

        // 记录正在运行的脚本
        tasks[task.pid] = task
        (task as DefaultScriptTask).future = task.future.thenApply {
            tasks.remove(task.pid) ?: warning("Running task $id not found.")
            return@thenApply it
        }

        return task.future
    }

    /**
     * 终止指定脚本的所有任务
     *
     * @param id 脚本 ID
     * */
    fun stop(id: String) {
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val (_, task) = iterator.next()
            if (task.script.id == id) {
                iterator.remove()
            }
        }
    }

    /**
     * 终止指定任务
     *
     * @param pid 任务 ID
     * */
    fun stop(pid: Long) {
        getTask(pid).terminate()
    }

    /**
     * 重载服务
     * */
    fun reload(): String {
        return service.reload()
    }

    internal fun nextPid(): Long {
        return ++pid
    }

    internal object Callback : ConfigServiceCallback {

        override fun onFileDeleted(id: String, file: File) {
            scripts.remove(id)
        }

        override fun onFileCreated(id: String, file: File) {
            val script = when (file.extension) {
                "ks" -> NativeScript(id, file)
                "yml", "yaml" -> CompiledScript(id, Configuration.loadFromFile(file))
                else -> error("Unsupported file extension ${file.extension}")
            }
            scripts[id] = script
        }

        override fun onFileModified(id: String, file: File) {
            when (val script = scripts[id]!!) {
                is NativeScript -> {
                    // 直接重新创建
                    onFileCreated(id, file)
                }
                is CompiledScript -> {
                    // 刷新配置
                    script.config.loadFromFile(file)
                }
                else -> error("Unknown script type ${script::class.java}")
            }
        }

        override fun onReloadCompleted(time: Double): String {
            return console().asLangText("module-script-service-load-succeeded", time)
        }

        override fun onReloadFailed(e: Throwable): String {
            return console().asLangText("module-script-service-load-failed", e.localizedMessage)
        }
    }

}