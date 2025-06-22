package top.lanscarlos.vulpecula.bacikal

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.module.configuration.ConfigNode
import taboolib.module.kether.ScriptActionParser
import taboolib.module.kether.ScriptService
import taboolib.platform.BukkitPlugin
import top.lanscarlos.vulpecula.bacikal.Bacikal.getBacikalQuest
import top.lanscarlos.vulpecula.bacikal.parser.BacikalContext
import top.lanscarlos.vulpecula.bacikal.parser.BacikalFruit
import top.lanscarlos.vulpecula.bacikal.parser.DefaultContext
import top.lanscarlos.vulpecula.bacikal.quest.*
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.config.DynamicSection
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:29
 */
object Bacikal {

    @ConfigNode("bacikal.analysis-symbol-closure")
    var analysisSymbolClosure: Boolean = true

    lateinit var service: BacikalService

    @Awake(LifeCycle.LOAD)
    private fun init() {
        service = DefaultBacikalService

        if (!ScriptService.locale.contains("bacikal")) {
            // 追加 kether.yml 文件
            val output = File(getDataFolder(), "kether.yml")
            if (!output.exists()) {
                warning("kether.yml not found.")
            }
            val resource = BukkitPlugin.getInstance().getResource("kether\$bacikal.yml")!!
            output.appendBytes(resource.readBytes())
            ScriptService.locale.reload()
        }
    }

    /**
     * 注册语句解析器
     * */
    fun <T> registerParser(func: BacikalContext.() -> BacikalFruit<T>): ScriptActionParser<T> {
        return ScriptActionParser {
            val context = DefaultContext(this)
            func(context)
        }
    }

    fun buildQuest(name: String, func: Consumer<BacikalQuestBuilder>): BacikalQuest {
        return service.buildQuest(name, func)
    }

    fun executeQuest(quest: BacikalQuest): CompletableFuture<*> {
        return service.executeQuest(quest)
    }

    fun terminateQuest(quest: BacikalQuest) {
        service.terminateQuest(quest)
    }

    /**
     * 构建任务
     *
     * @param name 任务名
     * */
    fun String.toBacikalQuest(name: String): BacikalQuest {
        return buildQuest(name) {
            it.appendContent(this@toBacikalQuest)
        }
    }

    /**
     * 构建任务
     *
     * @param name 任务名
     * */
    fun DynamicSection<*>.toBacikalQuest(
        name: String = this.config.path.toString().replace(File.separatorChar, '.') + "." + this.path
    ): BacikalQuest {
        return buildQuest(name) {
            it.appendContent(this@toBacikalQuest)
        }
    }

    /**
     * 获取任务
     *
     * @param path 节点路径
     * @param name 任务名
     * */
    fun DynamicConfig.getBacikalQuest(
        path: String,
        name: String = this.path.toString().replace(File.separatorChar, '.')
    ): BacikalQuest {
        return read(path).toBacikalQuest(name)
    }
}