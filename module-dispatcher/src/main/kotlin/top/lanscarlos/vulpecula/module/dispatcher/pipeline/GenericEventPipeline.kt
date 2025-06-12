package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.EventPipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12 10:11
 */
@Pipeline
class GenericEventPipeline : AbstractEventPipeline<Event>() {

    override fun process(context: Context) {
        TODO("Not yet implemented")
    }

    override fun preprocess(context: Context) {
        TODO("Not yet implemented")
    }

    override fun postprocess(context: Context) {
        TODO("Not yet implemented")
    }

}