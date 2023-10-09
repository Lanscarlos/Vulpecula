package top.lanscarlos.vulpecula.core.modularity

import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.FileWatcher
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.bacikal.BacikalScript
import top.lanscarlos.vulpecula.bacikal.BacikalWorkspace
import top.lanscarlos.vulpecula.bacikal.DefaultWorkspace
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.config.DefaultDynamicConfig
import top.lanscarlos.vulpecula.core.VulpeculaContext
import top.lanscarlos.vulpecula.core.utils.timing
import top.lanscarlos.vulpecula.modularity.ModularDispatcher
import top.lanscarlos.vulpecula.modularity.ModularHandler
import top.lanscarlos.vulpecula.modularity.Module
import java.io.File
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity
 *
 * @author Lanscarlos
 * @since 2023-08-30 12:35
 */
class DefaultModule(override val directory: File) : Module, Consumer<Pair<File, String>> {

    override val id: String = root.toPath().relativize(directory.toPath()).toString().replace(File.separatorChar, '.')

    val config = DefaultDynamicConfig(File(directory, "module.yml"))

    val automaticReloadDispatcher by config.readBoolean("automatic-reload.dispatcher", true)

    val automaticReloadHandler by config.readBoolean("automatic-reload.handler", true)

    override val dispatchers = mutableMapOf<String, ModularDispatcher>()

    override val handlers = mutableMapOf<String, ModularHandler>()

    override val workspace = DefaultWorkspace(File(directory, "build"))

    val quests: MutableMap<String, BacikalScript>
        get() = workspace.quests

    init {
        initHandlers()
        initDispatchers()

        // 正式启动模块
        enable()
    }

    override fun accept(t: Pair<File, String>) {
        val (file, type) = t
        when (type) {
            "dispatcher" -> {
                // 移除旧的调度器
                val iterator = dispatchers.iterator()
                while (iterator.hasNext()) {
                    val dispatcher = iterator.next().value
                    if (dispatcher.file == file) {
                        // 注销监听器
                        dispatcher.unregisterListener()
                        iterator.remove()

                        // 移除任务
                        quests.remove(dispatcher.id)
                    }
                }

                // 加载新的调度器
                val config = DefaultDynamicConfig(file)
                for (key in config.readKeys(false)) {
                    val dispatcher = DefaultDispatcher(this, key, config)
                    this.dispatchers[key] = dispatcher

                    // 构建任务
                    quests[dispatcher.id] = dispatcher.buildQuest()

                    // 注册监听器
                    dispatcher.registerListener()
                }
            }
            "handler" -> {
                // 所有受影响的调度器
                val affected = mutableSetOf<String>()

                // 移除旧的处理器
                val iterator = handlers.iterator()
                while (iterator.hasNext()) {
                    val handler = iterator.next().value
                    if (handler.file == file) {
                        // 注销监听器
                        iterator.remove()
                        affected += handler.bind
                    }
                }

                // 加载新的处理器
                val config = DefaultDynamicConfig(file)
                for (key in config.readKeys(false)) {
                    val handler = DefaultHandler(this, key, config)
                    this.handlers[key] = handler

                    // 将绑定的调度器加入缓存
                    affected += handler.bind
                }

                // 构建脚本
                for (dispatcher in dispatchers.values) {
                    if (dispatcher.id in affected) {
                        quests[dispatcher.id] = dispatcher.buildQuest()
                    }
                }
            }
        }
    }

    override fun enable() {
        dispatchers.values.forEach { it.registerListener() }

        // TODO 注册所有命令
        // TODO 注册所有日程任务
    }

    override fun disable() {
        // 注销所有监听器
        dispatchers.values.forEach { it.unregisterListener() }

        // TODO 注销所有命令
        // TODO 注销所有日程任务
        // TODO 终止所有正在运行的脚本
    }

    fun initHandlers() {
        val folder = File(directory, "handlers")
        if (!folder.exists() || !folder.isDirectory) {
            return
        }

        val queue = ArrayDeque<File>()
        queue.addAll(folder.listFiles() ?: return)
        while (queue.isNotEmpty()) {
            val file = queue.removeFirst()
            if (file.name[0] == '#') {
                continue
            }
            if (file.isDirectory) {
                queue.addAll(file.listFiles() ?: emptyArray())
                continue
            }
            if (!file.name.endsWith(".yml") && !file.name.endsWith(".yaml")) {
                continue
            }

            // 文件监听
            if (automaticReloadHandler) {
                FileWatcher.INSTANCE.addListener(file, file to "handler", this)
            } else {
                FileWatcher.INSTANCE.removeListener(file)
            }

            // 加载文件
            val config = DefaultDynamicConfig(file)
            for (key in config.readKeys(false)) {
                val handler = DefaultHandler(this, key, config)
                handlers[key] = handler
            }
        }
    }

    /**
     * 初始化事件调度器
     * */
    fun initDispatchers() {
        val folder = File(directory, "dispatchers")
        if (!folder.exists() || !folder.isDirectory) {
            return
        }

        val queue = ArrayDeque<File>()
        queue.addAll(folder.listFiles() ?: return)
        while (queue.isNotEmpty()) {
            val file = queue.removeFirst()
            if (file.name[0] == '#') {
                continue
            }
            if (file.isDirectory) {
                queue.addAll(file.listFiles() ?: emptyArray())
                continue
            }
            if (!file.name.endsWith(".yml") && !file.name.endsWith(".yaml")) {
                continue
            }

            // 文件监听
            if (automaticReloadDispatcher) {
                FileWatcher.INSTANCE.addListener(file, file to "dispatcher", this)
            } else {
                FileWatcher.INSTANCE.removeListener(file)
            }

            // 加载文件
            val config = DefaultDynamicConfig(file)
            for (key in config.readKeys(false)) {
                val dispatcher = DefaultDispatcher(this, key, config)
                dispatchers[key] = dispatcher

                // 构建任务
                quests[dispatcher.id] = dispatcher.buildQuest()
            }
        }
    }

    companion object {

        /**
         * 根目录
         * */
        val root = File(getDataFolder(), "modules")

        val registry = mutableMapOf<String, Module>()

        @Awake(LifeCycle.LOAD)
        fun onLoad() {
            VulpeculaContext.registerReloadable("module") {
                try {
                    val start = timing()
                    load()
                    console().asLangText("Module-Load-Succeeded", timing(start))
                } catch (ex: Exception) {
                    console().asLangText("Module-Load-Failed", ex.localizedMessage ?: "null")
                }
            }
        }

        /**
         * 加载模块
         * */
        fun load() {

            // 清空缓存
            registry.values.forEach {
                it.disable()
            }

            if (!root.exists()) {
                releaseResourceFile("modules/#def/module.yml", true)
            }

            /*
            * 通过广度优先搜索加载模块
            * 文件夹内含有 module.yml 文件的将视为模块，且不再向下搜索
            * */
            val queue = ArrayDeque<File>()
            queue.add(root)
            while (queue.isNotEmpty()) {
                val file = queue.removeFirst()
                if (!file.isDirectory || file.name[0] == '#') {
                    continue
                }
                if (File(file, "module.yml").exists()) {
                    val module = DefaultModule(file)
                    registry[module.id] = module
                } else {
                    queue.addAll(file.listFiles() ?: emptyArray())
                }
            }
        }

        @SubscribeEvent
        fun e(e: PlayerQuitEvent) {
            registry.values.forEach { module ->
                module.dispatchers.values.forEach { dispatcher ->
                    dispatcher.baffle.reset(e.player.uniqueId.toString())
                }
            }
        }
    }
}