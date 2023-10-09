package top.lanscarlos.vulpecula.core.modularity

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.ProxyListener
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common5.Baffle
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.bacikal.bacikalQuest
import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.core.VulpeculaContext
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
) : ModularDispatcher {

    override val file: File
        get() = config.file

    override val listen: Class<*> by config.read("$id.listen") { value ->
        VulpeculaContext.getClass(value.toString())
            ?: error("Invalid listen class: \"$value\"")
    }

    override val acceptCancelled: Boolean by config.readBoolean("$id.accept-cancelled", false)

    override val priority: EventPriority by config.read("$id.priority") { value ->
        EventPriority.values().firstOrNull { it.name.equals(value?.toString(), true) }
            ?: error("Invalid priority: \"$value\"")
    }

    override val namespace: List<String> by config.readStringList("$id.namespace", emptyList())

    override val variables: Any? by config.read("$id.variables")

    override val preprocessor: Any? by config.read("$id.preprocessor")

    override val postprocessor: Any? by config.read("$id.postprocessor")

    override val exceptions: Any? by config.read("$id.exceptions")

    override val playerReference: String? by config.readString("$id.player-ref")

    override val baffle: Baffle by config.read("$id.baffle") { value ->
        when (value) {
            is ConfigurationSection -> {
                when {
                    "time" in value -> {
                        val time = value.getInt("time", 0)
                        if (time <= 0) {
                            error("Invalid baffle time: \"$time\"")
                        }
                        Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                    }

                    "count" in value -> {
                        val count = value.getInt("count", 0)
                        if (count <= 0) {
                            error("Invalid baffle count: \"$count\"")
                        }
                        Baffle.of(count)
                    }

                    else -> error("Invalid baffle type.")
                }
            }

            is String -> {
                if (value.last() == 's') {
                    val time = value.substring(0, value.length - 1).toIntOrNull()
                        ?: error("Invalid baffle time: \"$value\"")
                    Baffle.of(time * 50L, TimeUnit.MILLISECONDS)
                } else {
                    val count = value.toIntOrNull() ?: error("Invalid baffle count: \"$value\"")
                    Baffle.of(count)
                }
            }

            else -> error("Invalid baffle type.")
        }
    }

    override val isRunning: Boolean
        get() = listener != null

    var listener: ProxyListener? = null

    override fun registerListener() {
        unregisterListener()
        listener = registerBukkitListener(listen, priority, !acceptCancelled) {
            call(it as? Event ?: return@registerBukkitListener)
        }
    }

    override fun unregisterListener() {
        taboolib.common.platform.function.unregisterListener(listener ?: return)
        listener = null
    }

    override fun buildQuest(): BacikalQuest {

        val name = file.relativeTo(module.directory).path.replace(File.separatorChar, '.') + "\$$id.ks"
        val artifact = File(module.directory, ".build/$name")

        // 构建任务
        return bacikalQuest(id) {
            artifactFile = artifact

            appendBaseBlock {
                appendVariables(this@DefaultDispatcher.variables)
                appendPreprocessor(this@DefaultDispatcher.preprocessor)
                appendPostprocessor(this@DefaultDispatcher.postprocessor)
                appendExceptions(this@DefaultDispatcher.exceptions)

                for (handler in module.handlers.values) {
                    if (this@DefaultDispatcher.id !in handler.bind) {
                        continue
                    }
                    appendLiteral("call ${handler.id}")
                    appendBlock(handler.id, handler.buildQuest().content)
                }
            }
        }
    }

    fun call(event: Event) {

    }
}