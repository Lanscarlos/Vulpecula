package top.lanscarlos.vulpecula.module.script

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.*
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.module.bacikal.exception.QuestCompileException
import top.lanscarlos.vulpecula.common.config.ConfigService
import top.lanscarlos.vulpecula.common.config.Configs
import top.lanscarlos.vulpecula.common.config.ConfigServiceCallback
import top.lanscarlos.vulpecula.common.config.ConfigStatistics
import top.lanscarlos.vulpecula.common.config.exception.ConfigFieldReadException
import top.lanscarlos.vulpecula.common.config.exception.UnsupportedFileExtensionException
import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.exception.InvalidTypeException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.module.script.exception.ScriptBlankException
import top.lanscarlos.vulpecula.module.script.exception.ScriptNotFoundException
import top.lanscarlos.vulpecula.module.script.exception.TaskNotFoundException
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.script
 *
 * @author Lanscarlos
 * @since 2025/4/25 10:00
 */
object ScriptService {

    internal val name: String get() = Lang.MODULE_SCRIPT_DISPLAY_NAME.asText(console())

    private val directory: File = File(getDataFolder(), "script")

    private val scripts: HashMap<String, Script> = hashMapOf()

    private val tasks: HashMap<Long, ScriptTask> = hashMapOf()

    private var pid: Long = 0

    private val rawMapping: HashMap<String, Script> = hashMapOf() // 用于映射原始文件 ID 与脚本的关系

    private val service: ConfigService = ConfigService("script", name, directory, 8, Callback)

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        // 自动注册配置服务
        Configs.register(service)
    }

    /**
     * 获取脚本
     *
     * @param id 脚本 ID
     * @throws ScriptNotFoundException 脚本不存在
     * @return 脚本
     * */
    fun get(id: String): Script = getOrNull(id) ?: throw ScriptNotFoundException(id)

    /**
     * 获取脚本
     *
     * @param id 脚本 ID
     * @return 脚本, 或 null
     * */
    fun getOrNull(id: String): Script? {
        return scripts[id]
    }

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
    fun getTask(pid: Long): ScriptTask = getTaskOrNull(pid) ?: throw TaskNotFoundException(pid)

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
     * @throws IllegalStateException 脚本不存在
     * @return 脚本
     * */
    fun compile(source: String): Script {
        if (source.isBlank()) {
            throw ScriptBlankException()
        }
        return if (source.getOrNull(0) == '@' && source.lowercase().startsWith("@script:")) {
            // 调用脚本
            val id = source.substring(8)
            get(id) // 检测 ID 是否存在
            ProxyScript(id)
        } else {
            NativeScript(source)
        }
    }

    /**
     * 运行指定脚本
     *
     * @param id 脚本 ID
     * @param sender 脚本执行者
     * @param args 脚本参数
     * @param variables 脚本变量
     * @param onSuccess 成功回调
     * @param onFailure 异常回调
     * @throws ScriptNotFoundException 脚本不存在
     * @return 运行结果
     * */
    fun run(
        id: String,
        sender: ProxyCommandSender? = null,
        args: List<Any?> = emptyList(),
        variables: Map<String, Any> = emptyMap()
    ): ScriptTask {
        return run(get(id), sender, args, variables)
    }

    /**
     * 运行脚本
     *
     * @param script 脚本
     * @param sender 脚本执行者
     * @param args 脚本参数
     * @param variables 脚本变量
     * @param onSuccess 成功回调
     * @param onFailure 异常回调
     * @return 运行结果
     * */
    fun run(
        script: Script,
        sender: ProxyCommandSender? = null,
        args: List<Any?> = emptyList(),
        variables: Map<String, Any> = emptyMap()
    ): ScriptTask {
        return script.run(sender, args, variables)
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

    /**
     * 移除任务
     * */
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
        getTask(pid).stop()
    }

    /**
     * 重载服务
     * */
    fun reload(sender: ProxyCommandSender) {
        service.load(sender)
    }

    internal fun nextPid(): Long {
        return ++pid
    }

    private object Callback : ConfigServiceCallback {

        override fun onFileCreated(sender: ProxyCommandSender, id: String, file: File) {
            val script = when (file.extension) {
                "ks" -> NativeScript(id, file)
                "yml", "yaml" -> CompiledScript(id, Configuration.loadFromFile(file))
                else -> throw UnsupportedFileExtensionException(file.extension)
            }
            rawMapping[id] = script
            require(!scripts.containsKey(script.id)) {
                throw ScriptConflictException(script.id, file)
            }
            scripts[script.id] = script
        }

        override fun onFileModified(sender: ProxyCommandSender, id: String, file: File) {
            when (val script = rawMapping[id]!!) {
                is NativeScript -> {
                    // 直接重新创建
                    onFileCreated(sender, id, file)
                }
                is CompiledScript -> {
                    scripts.remove(script.id)
                    // 刷新配置
                    script.config.loadFromFile(file)
                    // 刷新 id
                    require(!scripts.containsKey(script.id)) {
                        throw ScriptConflictException(script.id, file)
                    }
                    scripts[script.id] = script
                    // 重新构建脚本任务
                    script.rebuild()
                }
                else -> throw InvalidTypeException(script)
            }
        }

        override fun onFileDeleted(sender: ProxyCommandSender, id: String, file: File) {
            rawMapping.remove(id)
            scripts.remove(id)
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
            val message = (cause as? AbstractLocalizedException)?.getLocalizedMessage(sender) ?: cause.localizedMessage ?: ""
            Lang.MODULE_SCRIPT_LOAD_FAILURE.error(sender, id, message)
            if (cause is QuestCompileException) {
                cause.notice(sender)
            }
        }

        override fun onLoadInit(sender: ProxyCommandSender, directory: File) {
            releaseResourceFolder("script")
        }

        override fun onLoadAutomatic(sender: ProxyCommandSender, id: String, file: File, time: Double) {
            Lang.MODULE_SCRIPT_LOAD_AUTOMATIC.info(sender, id, time)
        }

        override fun onLoadSuccess(sender: ProxyCommandSender, statistics: ConfigStatistics) {
            Lang.MODULE_SCRIPT_LOAD_SUCCESS.info(sender, scripts.size, statistics.consumeTime)
        }

        override fun onLoadFailure(sender: ProxyCommandSender, e: Throwable) {
            // 加载器异常时需要清空已载入的对象
            rawMapping.clear()
            scripts.clear()
        }

    }

    class ScriptConflictException(scriptId: String, file: File) :
        DefaultLocalizedException(
            Lang.MODULE_SCRIPT_CONFLICT,
            arrayOf(scriptId, directory.toPath().relativize(file.toPath()))
        )

}