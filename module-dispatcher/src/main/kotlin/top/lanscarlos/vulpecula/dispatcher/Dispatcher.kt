package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.script.Script
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 9:22
 */
interface Dispatcher {

    val id: String

    val clazz: ReflexClass

    val priority: EventPriority

    val weight: Int

    val preprocessing: Script?

    val postprocessing: Script?

    val executable: Script

    fun accept(event: Event)

    fun reload(file: File)

}