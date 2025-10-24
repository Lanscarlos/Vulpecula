package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import taboolib.common5.Baffle
import taboolib.common5.Baffle.BaffleCounter
import taboolib.common5.Baffle.BaffleTime
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.applicative.IntApplicative
import top.lanscarlos.vulpecula.common.applicative.StringApplicative
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.core.utils.TimeUtil
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * 用于判断冷却是否通过
 *
 * @author Lanscarlos
 * @since 2025/6/13
 */
@AutoRegistered
class BafflePipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<Event>(clazz, config) {

    override val priority: Int = 128 // 分配较高的优先级用于优先处理冷却

    val counterBaffle: Baffle? by config.read("baffle-count").convert(::parseCounterBaffle)

    val timeBaffle: Baffle? by config.read("baffle-time").convert(::parseTimeBaffle)

    val cancel: Boolean by config.read("baffle-cancel").boolean(false)

    val global: Boolean by config.read("baffle-global").boolean(false)

    override fun filter(context: PipelineContext) {
        if (counterBaffle == null && timeBaffle == null) {
            return
        }
        val id = if (global) "*" else context.principalId
        if (counterBaffle?.hasNext(id, false) == true) {
            // 计数通过
            return
        }
        if (timeBaffle?.hasNext(id, false) == true) {
            // 冷却通过
            return
        }
        if (cancel) {
            context.cancel()
        } else {
            context.filter()
        }
        // TODO 阻断处理流的传播
    }

    override fun afterFilter(context: PipelineContext) {
        // 更新阻断器数据
        counterBaffle?.next()
        timeBaffle?.next()
    }

    private fun parseCounterBaffle(value: Any?): Baffle? {
        if (value == null) {
            return null
        }
        return BaffleCounter.of(IntApplicative.convert(value))
    }

    private fun parseTimeBaffle(value: Any?): Baffle? {
        if (value == null) {
            return null
        }
        return BaffleTime.of(TimeUtil.parse(StringApplicative.convert(value)), TimeUnit.MILLISECONDS)
    }

}