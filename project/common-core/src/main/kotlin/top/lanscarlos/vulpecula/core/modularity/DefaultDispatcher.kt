package top.lanscarlos.vulpecula.core.modularity

import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.bacikal.bacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.config.bindConfigSection
import top.lanscarlos.vulpecula.core.VulpeculaContext
import top.lanscarlos.vulpecula.modularity.DispatcherPipeline
import top.lanscarlos.vulpecula.modularity.ModularDispatcher
import top.lanscarlos.vulpecula.modularity.Module
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity
 *
 * @author Lanscarlos
 * @since 2023-08-29 16:42
 */
class DefaultDispatcher(
    override val module: Module,
    override val id: String,
    val config: DynamicConfig
) : ModularDispatcher, Runnable {

    override val file: File
        get() = config.file

    @Suppress("UNCHECKED_CAST")
    override val listen: Class<out Event> by config.read("$id.listen") { value ->
        VulpeculaContext.getClass(value.toString()) as? Class<out Event>
            ?: error("Invalid listen class: \"$value\" at dispatcher \"$id\"")
    }

    override val priority: EventPriority by config.read("$id.priority") { value ->
        EventPriority.values().firstOrNull { it.name.equals(value?.toString(), true) }
            ?: error("Invalid priority: \"$value\" at dispatcher \"$id\"")
    }

    override val acceptCancelled: Boolean by config.readBoolean("$id.accept-cancelled", false)

    override val namespace: List<String> by config.readStringList("$id.namespace", emptyList())

    override val preprocessing: Any? by config.read("$id.before-handle")

    override val postprocessing: Any? by config.read("$id.after-handle")

    override val handlers: List<String> by config.readStringList("$id.handlers", emptyList())

    override val exceptions: Any? by config.read("$id.exceptions")

    override val baffle: Baffle? by config.read("$id.baffle") { value ->
        when (value) {
            is ConfigurationSection -> {
                when {
                    "time" in value -> {
                        val time = value.getInt("time", 0)
                        if (time <= 0) {
                            error("Invalid baffle time: \"$time\" at dispatcher \"$id\"")
                        }
                        Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                    }

                    "count" in value -> {
                        val count = value.getInt("count", 0)
                        if (count <= 0) {
                            error("Invalid baffle count: \"$count\" at dispatcher \"$id\"")
                        }
                        Baffle.of(count)
                    }

                    else -> error("Invalid baffle type at dispatcher \"$id\", must be \"time\" or \"count\"")
                }
            }

            is String -> {
                if (value.matches("\\d+(\\.\\d+)?[sStT]".toRegex())) {
                    // 匹配时间, 例如: 1s 1.5s 1S 1.5S
                    val time = value.substring(0, value.length - 1).toIntOrNull()
                        ?: error("Invalid baffle time: \"$value\" at dispatcher \"$id\"")
                    Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                } else {
                    val count = value.toIntOrNull() ?: error("Invalid baffle count: \"$value\" at dispatcher \"$id\"")
                    Baffle.of(count)
                }
            }

            else -> null
        }
    }

    override val pipelines: List<DispatcherPipeline<in Event>> = AbstractPipeline.generate(listen, this.config)

    private val artifact: File? by config.read("$id.export") { value ->
        val path = value?.toString() ?: return@read null
        if (path.isEmpty() || path.isBlank()) {
            error("Invalid export path: \"$path\" at dispatcher \"$id\"")
        }
        val file = when (path[0]) {
            '.', '/' -> File(file, path.substring(1))
            '~' -> File(module.directory, path.substring(1))
            else -> File(file, path)
        }
        if (file.isDirectory) {
            error("Invalid export path: \"$path\" at dispatcher \"$id\", must be a file!")
        }
        file
    }

    private var quest: BacikalQuest

    override val isRunning: Boolean
        get() = listener != null

    private var listener: ProxyListener? = null

    init {
        config.onAfterReload(this)
        quest = buildQuest()
    }

    /**
     * 配置文件重载后执行
     * */
    override fun run() {
        TODO("Not yet implemented")
    }

    override fun registerListener() {
        unregisterListener()
        listener = registerBukkitListener(listen, priority, !acceptCancelled) {
            process(it)
        }
    }

    override fun unregisterListener() {
        taboolib.common.platform.function.unregisterListener(listener ?: return)
        listener = null
    }

    /**
     * 构建任务
     * */
    fun buildQuest(): BacikalQuest {
        return bacikalQuest(id) {
            artifactFile = artifact

            appendBaseBlock {
                if (ACTION_SCRIPT_HEADER != null) {
                    // 如果存在脚本语句, 则添加脚本语句
                    for (handler in handlers) {
                        appendLiteral("$ACTION_SCRIPT_HEADER run $handler")
                    }
                }
                appendPreprocessor(preprocessing)
                appendPostprocessor(postprocessing)
            }
        }
    }

    /**
     * 处理事件
     * */
    fun process(event: Event) {
        // 创建脚本上下文环境
        val context = quest.createContext()

        // 流水线处理
        for (pipeline in pipelines) {
            // 过滤事件
            if (!pipeline.filter(event)) {
                return
            }

            // 前置处理
            pipeline.preprocess(event)

            // 预设变量
            for (variable in pipeline.variables(event)) {
                context.setVariable(variable.key, variable.value)
            }
        }

        // 获取玩家对象
        val player = context.getVariable<Player>(AbstractPipeline.VARIABLE_PLAYER)

        // 阻断检查
        baffle?.let { baffle ->
            val key = player?.name ?: event.eventName
            if (!baffle.hasNext(key)) return
        }

        // 设置脚本执行者
        context.sender = player?.let { adaptCommandSender(it) }

        // 执行脚本
        context.runActions()
    }

    companion object {

        /**
         * 脚本语句
         * */
        val ACTION_SCRIPT_HEADER by bindConfigSection("script", "action-registry.yml") { value ->
            val section = value as? ConfigurationSection ?: return@bindConfigSection null
            if (section.getBoolean("disable", false)) {
                // 脚本语句被禁用
                return@bindConfigSection null
            }
            when (val content = section["local"]) {
                is String -> content
                is List<*> -> content.firstOrNull()?.toString()
                else -> null
            }
        }

    }
}