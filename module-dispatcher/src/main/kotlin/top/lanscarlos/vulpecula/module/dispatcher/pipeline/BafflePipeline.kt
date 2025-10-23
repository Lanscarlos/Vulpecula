package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import taboolib.common5.Baffle
import taboolib.common5.Baffle.BaffleCounter
import taboolib.common5.Baffle.BaffleTime
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.core.exception.InvalidTypeException
import top.lanscarlos.vulpecula.common.core.utils.TimeUtil
import top.lanscarlos.vulpecula.module.dispatcher.Context
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

    val baffle: Baffle? by config.read("baffle").convert(::parseBaffle)

    val cancel: Boolean by config.read("baffle-cancel").boolean(false)

    val global: Boolean by config.read("baffle-global").boolean(false)

    override fun filter(context: Context) {
        val baffle = this.baffle ?: return
        val id = if (global) "*" else context.principalId
        if (baffle.hasNext(id, false)) {
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

    override fun afterFilter(context: Context) {
        // 更新阻断器数据
        baffle?.next()
    }

    private fun parseBaffle(value: Any?): Baffle? {
        if (value == null) {
            return null
        }
        return when (value) {
            is Number -> BaffleCounter.of(value.toInt())
            is String -> BaffleTime.of(TimeUtil.parse(value), TimeUnit.MILLISECONDS)
            else -> throw InvalidTypeException(value)
        }
    }

}