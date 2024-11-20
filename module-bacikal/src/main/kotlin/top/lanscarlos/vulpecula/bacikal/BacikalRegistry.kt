package top.lanscarlos.vulpecula.bacikal

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getOpenContainers
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.library.kether.QuestActionParser
import taboolib.library.reflex.ClassMethod
import taboolib.library.reflex.ReflexClass
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.kether.Kether
import taboolib.module.kether.StandardChannel
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2024-11-20 16:33
 */
@Awake(LifeCycle.LOAD)
object BacikalRegistry : ClassVisitor(-1) {

    override fun visitStart(clazz: ReflexClass) {
    }

    fun registerAction(method: ClassMethod, instance: Supplier<*>?) {
    }

    /**
     * 注册语句
     */
    fun registerAction(id: String, parser: QuestActionParser) {

    }

    /**
     * 注册语句
     *
     * @param local 本地注册信息 namespace to name
     * @param remote 远程注册信息 namespace to name
     */
    fun registerAction(id: String, parser: QuestActionParser, local: List<Pair<String, String>>, remote: List<Pair<String, String>>) {
        info("Registering action \"$id\":")
        for ((namespace, name) in local) {
            Kether.scriptRegistry.registerAction(namespace, name, parser)
            info("    - Local($pluginId) $namespace:$name")
        }

        val map = remote.groupBy({ it.first }, { it.second })
        for (connection in getOpenContainers()) {
            if (connection.name == pluginId) {
                // 过滤自身插件
                continue
            }

            for ((namespace, name) in map) {
                connection.call(
                    StandardChannel.REMOTE_ADD_ACTION,
                    arrayOf(pluginId, name, namespace)
                )
                info("    - Remote(${connection.name}) $namespace:$name")
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

}