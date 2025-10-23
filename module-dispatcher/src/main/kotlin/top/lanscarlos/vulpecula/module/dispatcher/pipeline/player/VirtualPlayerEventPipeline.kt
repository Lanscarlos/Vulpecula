package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.PipelineContext
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AbstractPipeline

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/7/29
 */
abstract class VirtualPlayerEventPipeline<T: Event>(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<T>(clazz, config) {

    override fun filter(context: PipelineContext) {
        if (context.player == null) {
            // 玩家必须存在
            context.filter()
        }
    }

}