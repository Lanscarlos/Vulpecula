package top.lanscarlos.vulpecula.module.script

import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.*
import taboolib.module.configuration.Configuration
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.bacikal.quest.BacikalCompileException
import top.lanscarlos.vulpecula.bacikal.quest.BacikalRuntimeException
import top.lanscarlos.vulpecula.common.config.ConfigLoadContext
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function
import kotlin.math.min

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

    @Awake(LifeCycle.LOAD)
    fun onEnable() {
        // 自动注册配置服务
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
     * 编译指定内容为脚本, 本次编译不会被记录
     *
     * @param source 源码
     * @return 脚本
     * */
    fun compile(source: String): Script {
        return NativeScript(source)
    }

    /**
     * 编译指定内容为脚本, 本次编译不会被记录
     *
     * @param source 源码
     * @param id 脚本 ID
     * @return 脚本
     * */
    fun compile(source: String, id: String): Script {
        return NativeScript(id, source)
    }

    /**
     * 运行指定脚本
     *
     * @param id 脚本 ID
     * @param player 玩家
     * @param args 脚本参数
     * @throws IllegalStateException 脚本不存在
     * @return 运行结果
     * */
    fun run(
        id: String,
        player: Player,
        args: List<Any?>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return run(get(id), adaptPlayer(player), args, onSuccess, onFailure)
    }

    /**
     * 运行指定脚本
     *
     * @param id 脚本 ID
     * @param player 玩家
     * @param args 脚本参数
     * @throws IllegalStateException 脚本不存在
     * @return 运行结果
     * */
    fun run(
        id: String,
        player: Player,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return run(get(id), adaptPlayer(player), args, onSuccess, onFailure)
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
    fun run(
        id: String,
        sender: ProxyCommandSender?,
        args: List<Any?>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return run(get(id), sender, args, onSuccess, onFailure)
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
    fun run(
        id: String,
        sender: ProxyCommandSender?,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return run(get(id), sender, args, onSuccess, onFailure)
    }

    /**
     * 运行脚本
     *
     * @param script 脚本
     * @param sender 脚本执行者
     * @param args 脚本参数
     * @return 运行结果
     * */
    fun run(
        script: Script,
        sender: ProxyCommandSender?,
        args: List<Any?>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return script.execute(sender, args, onSuccess, onFailure).future
    }

    /**
     * 运行脚本
     *
     * @param script 脚本
     * @param sender 脚本执行者
     * @param args 脚本参数
     * @return 运行结果
     * */
    fun run(
        script: Script,
        sender: ProxyCommandSender?,
        args: Map<String, Any>,
        onSuccess: Consumer<Any?>,
        onFailure: Function<BacikalRuntimeException, Any?>
    ): CompletableFuture<Any?> {
        return script.execute(sender, args, onSuccess, onFailure).future
    }

    /**
     * 追踪运行的脚本
     * */
    internal fun trackTask(task: ScriptTask) {
        if (task.isDone) {
            return
        }
        // 追踪正在运行的脚本
        tasks[task.pid] = task
    }

    internal fun clearTask(pid: Long) {
//        tasks.remove(pid) ?: warning("Running task $pid not found.")
        tasks.remove(pid) // 已完成的任务不会被追踪, 因此任务 pid 可能不一定存在
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
    fun reload(): ConfigLoadContext {
        val context = ConfigLoadContext()
        service.load(context)
        return context
    }

    internal fun nextPid(): Long {
        return ++pid
    }

    private object Callback : ConfigServiceCallback {

        override fun onFileDeleted(context: ConfigLoadContext, id: String, file: File) {
            scripts.remove(id)
        }

        override fun onFileCreated(context: ConfigLoadContext, id: String, file: File) {
            val script = when (file.extension) {
                "ks" -> NativeScript(id, file)
                "yml", "yaml" -> CompiledScript(id, Configuration.loadFromFile(file))
                else -> error("Unsupported file extension ${file.extension}")
            }
            scripts[id] = script
        }

        override fun onFileModified(context: ConfigLoadContext, id: String, file: File) {
            when (val script = scripts[id]!!) {
                is NativeScript -> {
                    // 直接重新创建
                    onFileCreated(context, id, file)
                }
                is CompiledScript -> {
                    // 刷新配置
                    script.config.loadFromFile(file)
                    // 重新构建脚本任务
                    script.rebuild()
                }
                else -> error("Unknown script type ${script::class.java}")
            }
        }

        override fun onLoadInit(context: ConfigLoadContext, directory: File) {
            releaseResourceFolder("script")
        }

        override fun onLoadCompleted(context: ConfigLoadContext, time: Double) {
            context.logs += console().asLangText("module-script-service-load-success", scripts.size, time)
        }

        override fun onLoadFailed(context: ConfigLoadContext, id: String, file: File, e: Throwable) {
            when (e) {
                is BacikalCompileException -> {
                    context.logs += console().asLangText("module-script-service-load-failure", id).split('\n')
                    context.logs += console().asLangText("module-script-service-load-failure-reason", e.localizedMessage)
                    val builder = StringBuilder(console().asLangText("module-script-service-load-failure-detail"))
                    val parsed = e.parsedContent.split('\n').filter { it.isNotEmpty() }
                    val unparse = e.unparseContent.split('\n').filter { it.isNotEmpty() }
                    var index = 0
                    for (i in 0 until 2) {
                        val line = parsed.getOrNull(parsed.size - 3 + i) ?: continue
                        val displayIndex = String.format("%3d", ++index)
                        val color = console().asLangText("module-script-service-load-failure-detail-parsed")
                        val content = console().asLangText("module-script-service-load-failure-detail-format", displayIndex, color + line)
                        builder.append('\n').append(content)
                    }
                    also {
                        val line = parsed.lastOrNull() ?: return@also
                        val displayIndex = String.format("%3d", ++index)
                        val color = console().asLangText("module-script-service-load-failure-detail-warning")
                        val content = console().asLangText("module-script-service-load-failure-detail-format", displayIndex, color + line)
                        builder.append('\n').append(content)
                    }
                    println("unparse >> $unparse")
                    for (i in 0 .. 5 - index) {
                        val line = unparse.getOrNull(i) ?: break
                        println("i >> $i")
                        if (i == 0) {
                            val color = console().asLangText("module-script-service-load-failure-detail-error")
                            builder.append(color).append(line)
                            continue
                        }
                        val displayIndex = String.format("%3d", i + index)
                        val color = console().asLangText("module-script-service-load-failure-detail-error")
                        val content = console().asLangText("module-script-service-load-failure-detail-format", displayIndex, color + line)
                        builder.append('\n').append(content)
                    }
                    context.logs += builder.toString()
                }
                else -> {
                    context.logs += console().asLangText("module-script-service-load-failure", e.localizedMessage)
                }
            }
        }
    }

}