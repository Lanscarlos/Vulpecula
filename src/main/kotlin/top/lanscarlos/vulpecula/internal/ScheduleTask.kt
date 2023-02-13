package top.lanscarlos.vulpecula.internal

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.*
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common5.cbool
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.kether.Script
import taboolib.module.kether.parseKetherScript
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.config.VulConfig
import top.lanscarlos.vulpecula.script.ScriptCompiler
import top.lanscarlos.vulpecula.utils.*
import top.lanscarlos.vulpecula.utils.Debug.debug
import java.io.File
import java.text.SimpleDateFormat
import java.time.Duration

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.internal.schedule
 *
 * @author Lanscarlos
 * @since 2022-12-15 11:22
 */
class ScheduleTask(
    val id: String,
    val path: String, // 所在文件路径
    val wrapper: VulConfig
) : ScriptCompiler {

    val dateFormat by wrapper.read("date-format") {
        SimpleDateFormat(it?.toString() ?: defDateFormat)
    }

    val autoStart by wrapper.readBoolean("auto-start", true)

    val async by wrapper.readBoolean("async", false)

    val startOf by wrapper.read("start") {
        if (it == null) return@read 0L
        try {
            dateFormat.parse(it.toString()).time
        } catch (ignored: Exception) {
            -1L
        }
    }

    val endOf by wrapper.read("end") {
        if (it == null) return@read 0L
        try {
            dateFormat.parse(it.toString()).time
        } catch (ignored: Exception) {
            -1L
        }
    }

    val delay by wrapper.readInt("delay", 0)

    val duration by wrapper.read("period") {
        if (it == null) return@read Duration.ZERO
        val pattern = "\\d+[dhms]".toPattern()
        val matcher = pattern.matcher(it.toString())
        var seconds = 0L
        while (matcher.find()) {
            val found = matcher.group()
            val number = found.substring(0, found.lastIndex).toLongOrNull() ?: 0
            when (found.last().uppercaseChar()) {
                'D' -> seconds += Duration.ofDays(number).seconds
                'H' -> seconds += number * 3600
                'M' -> seconds += number * 60
                'S' -> seconds += number
            }
        }
        Duration.ofSeconds(seconds)
    }

    val namespace by wrapper.readStringList("namespace", listOf("vulpecula"))

    val condition by wrapper.read("condition") {
        if (it != null) buildSection(it) else StringBuilder()
    }
    val deny by wrapper.read("deny") {
        if (it != null) buildSection(it) else StringBuilder()
    }
    val executable by wrapper.read("execute") {
        if (it != null) buildSection(it) else StringBuilder()
    }
    val exception by wrapper.read("exception") {
        if (it != null) buildException(it) else emptyList()
    }

    lateinit var source: StringBuilder
    lateinit var script: Script

    private var task: PlatformExecutor.PlatformTask? = null

    val isRunning get() = task != null
    val isStopped get() = task == null
    val period get() = (duration.toMillis() / 50L)
    val startDate get() = if (startOf > 0) dateFormat.format(startOf) else "Undefined"
    val endDate get() = if (endOf > 0) dateFormat.format(endOf) else "Undefined"

    init {
        // 编译脚本
        compileScript()
    }

    override fun buildSource(): StringBuilder {
        val builder = StringBuilder()

        /* 构建核心语句 */
        builder.append(executable)

        /* 构建异常处理 */
        if (exception.isNotEmpty() && (exception.size > 1 || exception.first().second.isNotEmpty())) {
            // 提取先前所有内容
            val content = builder.extract()
            compileException(builder, content, exception)
        }

        /* 构建条件处理 */
        if (condition.isNotEmpty()) {
            // 提取先前所有内容
            val content = builder.extract()
            compileCondition(builder, content, condition, deny)
        }

        /*
        * 收尾
        * 构建方法体
        * */

        // 提取先前所有内容
        val content = builder.extract()

        // 构建方法体
        builder.append("def main = {\n")
        builder.appendWithIndent(content, suffix = "\n")
        builder.append("}")

        // 消除注释
        eraseComment(builder)

        return builder
    }

    override fun compileScript() {
        try {
            // 尝试构建脚本
            val source = buildSource()
            this.script = source.toString().parseKetherScript(namespace.plus("vulpecula"))

            // 编译通过
            this.source = source

            debug(Debug.HIGHEST, "schedule \"$id\" build source:\n$source")
        } catch (e: Exception) {
            e.printKetherErrorMessage()
        }
    }

    /**
     * 开始任务
     * */
    fun runTask(args: Array<Any?> = emptyArray()) {
        // 取消上一次未结束的任务
        terminate()

        if (!::script.isInitialized) {
            // 未指定运行内容
            console().sendLang("Schedule-Execution-Undefined", id)
            return
        }

        val now = System.currentTimeMillis()

        // 已超过结束时间
        if (endOf in 1..now) {
            debug("ScheduleTask $id has completed. {now=${dateFormat.format(now)}, end-of=${dateFormat.format(endOf)}}")
            return
        }

        val period = if (!duration.isZero) duration.toMillis() else 0L
        val delay = if (now < startOf) {
            // 未达到开始时间
            startOf - now
        } else {
            // 已达到开始时间
            if (!duration.isZero) {
                /* 循环任务 */
                // 获取过去的循环次数 + 1
                val times = (now - startOf) / period + 1
                // 计算与下一次任务的间隔时间
                (startOf + times * period) - now
            } else {
                /*
                * 非循环任务
                * 直接开始
                * */
                0L
            }
        }

        debug("ScheduleTask $id ready to run. {async=$async, delay=${delay/50L}, period=${period/50L}, start-of=${dateFormat.format(startOf)}}")

        // 开始新的任务
        task = submit(
            async = async,
            period = period / 50L,
            /* 额外延迟 10 tick 是为了矫正时间的显示 */
            delay = (delay / 50L + this.delay + 10).coerceAtLeast(0)
        ) {
            if (endOf > 0 && System.currentTimeMillis() >= endOf) {
                debug("ScheduleTask $id has completed. {end-of=${dateFormat.format(endOf)}}")
                terminate()
                return@submit
            }

            debug("ScheduleTask $id running...")

            script.runActions {
                if (args.isEmpty()) return@runActions
                for ((i, arg) in args.withIndex()) {
                    rootFrame().variables().set("arg$i", arg)
                }
                rootFrame().variables().set("args", args)
            }
        }
    }

    /**
     * 终止任务
     * */
    fun terminate() {
        task?.cancel()
        task = null
    }

    /**
     * 对照并尝试更新
     * */
    fun contrast(section: ConfigurationSection) {
        var refresh = false // 是否更新脚本
        var restart = false // 是否重启任务

        wrapper.updateSource(section).forEach {
            when (it.first) {
                "namespace", "execute" -> {
                    refresh = true
                }
                "async", "period", "start", "end", "duration" -> {
                    restart = true
                }
            }
        }

        if (refresh) compileScript()
        if (restart) runTask()

        if (refresh || restart) {
            debug(Debug.HIGH, "ScheduleTask updated \"$id\"")
        }
    }

    companion object {

        val automaticReload by bindConfigNode("automatic-reload.schedule") {
            it?.cbool ?: false
        }

        val defDateFormat by bindConfigNode("schedule-setting.date-format") {
            it?.toString() ?: "yyyy-MM-dd HH:mm:ss"
        }

        val folder = File(getDataFolder(), "schedules")
        val cache = mutableMapOf<String, ScheduleTask>()

        fun get(id: String) = cache[id]

        @Awake(LifeCycle.ACTIVE)
        fun onActive() {
            cache.values.forEach {
                if (it.autoStart) it.runTask()
            }
        }

        private fun onFileChanged(file: File) {
            if (!automaticReload) {
                file.removeWatcher()
                return
            }

            val start = timing()
            try {
                var counter = 0
                val path = file.canonicalPath
                val config = file.toConfig()
                val keys = config.getKeys(false).toMutableSet()

                // 遍历已存在的任务
                val iterator = cache.iterator()
                while (iterator.hasNext()) {
                    val task = iterator.next().value
                    if (task.path != path) continue

                    if (task.id in keys) {
                        // 任务仍然存在于文件中，尝试更新任务属性
                        config.getConfigurationSection(task.id)?.let { section ->
                            if (section.getBoolean("disable", false)) return@let null

                            debug(Debug.HIGH, "ScheduleTask contrasting \"${task.id}\"")
                            task.contrast(section)
                            counter += 1
                        } ?: let {
                            // 节点寻找失败，删除任务
                            task.terminate()
                            iterator.remove()
                            debug(Debug.HIGH, "ScheduleTask delete \"${task.id}\"")
                        }

                        // 移除该 id
                        keys -= task.id
                    } else {
                        // 该任务已被用户删除
                        task.terminate()
                        iterator.remove()
                        debug(Debug.HIGH, "ScheduleTask delete \"${task.id}\"")
                    }
                }

                // 遍历新的任务
                for (key in keys) {

                    // 检查 id 冲突
                    if (key in cache) {
                        val conflict = cache[key]!!
                        console().sendLang("Schedule-Load-Failed-Conflict", key, conflict.path, path)
                        continue
                    }

                    config.getConfigurationSection(key)?.let { section ->
                        if (section.getBoolean("disable", false)) return@let

                        cache[key] = ScheduleTask(key, path, section.wrapper()).also {
                            // 启动新任务
                            it.runTask()
                        }
                        counter += 1
                        debug(Debug.HIGH, "ScheduleTask loaded \"$key\"")
                    }
                }

                console().sendLang("Schedule-Load-Automatic-Succeeded", file.name, counter, timing(start))
            } catch (e: Exception) {
                console().sendLang("Schedule-Load-Automatic-Failed", file.name, e.localizedMessage, timing(start))
            }
        }

        /**
         * @param init 是否为初始化操作
         * */
        fun load(init: Boolean): String {
            val start = timing()
            return try {

                // 暂停所有任务
                if (cache.isNotEmpty()) {
                    cache.values.forEach { it.terminate() }
                }

                // 清除缓存
                cache.clear()

                folder.ifNotExists {
                    releaseResourceFile("schedules/#def.yml", true)
                }.getFiles().forEach { file ->

                    val path = file.canonicalPath

                    // 添加文件监听器
                    if (automaticReload) {
                        file.addWatcher(false) { onFileChanged(this) }
                    }

                    // 加载文件
                    file.toConfig().forEachSection { key, section ->
                        if (section.getBoolean("disable", false)) return@forEachSection

                        // 检查 id 冲突
                        if (key in cache) {
                            val conflict = cache[key]!!
                            console().sendLang("Schedule-Load-Failed-Conflict", key, conflict.path, path)
                            return@forEachSection
                        }

                        cache[key] = ScheduleTask(key, path, section.wrapper())
                        debug(Debug.HIGH, "ScheduleTask loaded \"$key\"")
                    }
                }

                if (!init) {
                    // 重新启动全部任务
                    cache.values.forEach { it.runTask() }
                }

                console().asLangText("Schedule-Load-Succeeded", cache.size, timing(start)).also {
                    console().sendMessage(it)
                }
            } catch (e: Exception) {
                console().asLangText("Schedule-Load-Failed", e.localizedMessage, timing(start)).also {
                    console().sendMessage(it)
                }
            }
        }
    }
}