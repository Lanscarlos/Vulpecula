package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.kether.StandardChannel
import top.lanscarlos.vulpecula.module.bacikal.action.ActionClassRegister
import top.lanscarlos.vulpecula.module.bacikal.parser.ReflexActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ComplexActionParser
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
 *
 * 注册中心
 *
 * @author Lanscarlos
 * @since 2024-11-20 16:33
 */
@Awake(LifeCycle.LOAD)
object BacikalRegistry {

    @Config("bacikal-registry.yml")
    lateinit var registry: Configuration
        private set

    val headers = mutableMapOf<String, ComplexActionParser>()

    val metadata = mutableMapOf<String, Array<String>>()

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        // 注册拓展语句
        val folder = File(getDataFolder(), "action")
        if (!folder.exists()) {
            releaseResourceFolder("action")
        }

        for (file in folder.listFiles()) {
            if (!file.exists() || !file.isFile || !file.canRead()) {
                continue
            }
            if (file.extension != "jar") {
                continue
            }
            ActionClassRegister.registerAction(file)
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        for ((id, parser) in headers) {
            ActionClassRegister.registerAction(id, parser)
        }
    }

    /**
     * 注册语句
     *
     * @param parser 语句解析器
     * */
    fun registerAction(parser: ComplexActionParser) {
        for (action in parser.actions.values) {
            if (action is ComplexActionParser) {
                registerAction(action)
            } else {
                registerAction(action)
            }
        }
    }

    /**
     * 注册语句
     *
     * @param parser 语句解析器
     * */
    fun registerAction(parser: ReflexActionParser) {
        val names = listOf(parser.id).plus(parser.aliases)

        // 本地注册
        for (name in names) {
            Kether.scriptRegistry.registerAction("vulpecula", name, parser)
        }

        // 远程注册
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                // 过滤自身插件
                continue
            }
            connection.call(StandardChannel.REMOTE_ADD_ACTION, arrayOf(pluginId, names, "vulpecula"))
        }
    }

}