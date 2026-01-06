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
import top.lanscarlos.vulpecula.common.exception.DefaultLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang
import top.lanscarlos.vulpecula.common.utils.TimeUtil
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
@AutoRegistered("~")
class BafflePipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<Event>(clazz, config) {

    override val priority: Int = 128 // 分配较高的优先级用于优先处理冷却

    val countBaffle: Baffle? by config.read("baffle-count").convert(::parseCounterBaffle)

    val timeBaffle: Baffle? by config.read("baffle-time").convert(::parseTimeBaffle)

    val cancel: Boolean by config.read("baffle-cancel").boolean(false)

    val global: Boolean by config.read("baffle-global").boolean(false)

    init {
        if (countBaffle != null && timeBaffle != null) {
            // 不允许同时设置两种阻断器
            throw BaffleConflictException()
        }
    }

    override fun filter(context: PipelineContext) {
        val baffle = countBaffle ?: timeBaffle ?: return
        val id = if (global) "*" else context.principalId

        when (baffle) {
            is BaffleCounter -> {
                if (baffle.hasNext(id)) {
                    // 条件通过
                    return
                }
            }
            is BaffleTime -> {
                if (baffle.hasNext(id, false)) {
                    // 条件通过
                    return
                }
            }
            else -> error("Baffle type ${baffle::class.java.name} not supported.")
        }

        // 条件不通过, 阻断事件
        if (cancel) {
            context.cancel()
        } else {
            context.filter()
        }
        context.baffleFilter()
    }

    override fun afterFilter(context: PipelineContext) {
        // 更新阻断器数据
        val id = if (global) "*" else context.principalId
        timeBaffle?.next(id)
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
        val time = TimeUtil.parse(StringApplicative.convert(value))
        return BaffleTime.of(time, TimeUnit.MILLISECONDS)
    }

    class BaffleConflictException : DefaultLocalizedException(Lang.MODULE_DISPATCHER_BAFFLE_CONFLICT, arrayOf("?"))

}