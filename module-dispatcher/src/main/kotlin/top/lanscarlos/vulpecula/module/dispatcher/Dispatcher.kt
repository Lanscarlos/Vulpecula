package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.script.Script
import java.io.File
import java.util.function.Consumer

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 9:22
 */
interface Dispatcher : Consumer<Event> {

    val id: String

    val clazz: ReflexClass

    val priority: EventPriority

    val weight: Int

    val preprocessing: Script?

    val postprocessing: Script?

    val executable: Script

    fun reload(file: File)

    fun enable()

    fun disable()

    fun dispose()

}