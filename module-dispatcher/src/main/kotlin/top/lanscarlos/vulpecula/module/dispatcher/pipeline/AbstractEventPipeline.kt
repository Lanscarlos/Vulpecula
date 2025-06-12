package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.EventPipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12 11:56
 */
abstract class AbstractEventPipeline<T: Event> : EventPipeline<T> {

    @Suppress("UNCHECKED_CAST")
    fun getEvent(context: Context): T {
        return context.event as T
    }

}