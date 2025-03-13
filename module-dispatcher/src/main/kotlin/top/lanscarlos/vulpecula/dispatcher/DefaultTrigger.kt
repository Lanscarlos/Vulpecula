package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.*
import top.lanscarlos.vulpecula.dispatcher.condition.Condition
import top.lanscarlos.vulpecula.dispatcher.condition.Conditions
import top.lanscarlos.vulpecula.dispatcher.rule.Rule
import top.lanscarlos.vulpecula.dispatcher.rule.Rules
import java.lang.reflect.Field

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025-03-12 09:34
 */
class DefaultTrigger(val config: Configuration) : Trigger {

    override val clazz: Class<out Event> by config.read("listen-event").stringOrNull().convert(::parseEventClass)

    override val priority: EventPriority by config.read("listen-priority").string("NORMAL").convert(::parseEventPriority)

    val listenCancelled: Boolean by config.read("listen-cancelled").boolean(false)

    val playerRequired: Boolean by config.read("rule.player-required").boolean(true)

    val playerReference: String by config.read("rule.player-reference").string("@")

    val playerField: Field? by lazy(::parsePlayerField)

    val cooldown: Long by config.read("rule.cooldown").convert(::parseCooldown)

    override val weight: Int by config.read("weight").int(8)

    val rule: Rule<Event> by config.read("rule").convert(::parseRule)

    val filter: Condition by config.read("filter").convert(::parseCondition)

    val baffle: Condition by config.read("baffle").convert(::parseCondition)

    /**
     * 监听冷却
     * */
    private var time: Long = -1L

    override fun accept(event: Event) {
        // 事件取消检查
        if ((event as? Cancellable)?.isCancelled == true && !listenCancelled) {
            return
        }

        // 监听冷却检查
        if (time > 0L && time >= System.currentTimeMillis()) {
            return
        }

        val player = parsePlayer(event)
        // 玩家字段检查
        if (playerRequired && player == null) {
            return
        }

        // 创建上下文
        val context = Context(event, player)

        if (!rule.matches(context)) {
            // 规则不匹配, 过滤
            return
        }

        if (!filter.check(context)) {
            // 过滤事件
            return
        }

        if (!baffle.check(context)) {
            // 阻断事件
            (event as? Cancellable)?.isCancelled = true
            return
        }

        // 更新监听冷却
        time = System.currentTimeMillis() + cooldown

        // TODO 触发脚本
    }

    private fun parsePlayer(event: Event): Player? {
        return if (playerRequired) {
            (playerField?.get(event) ?: event.getProperty(playerReference)) as? Player
        } else {
            null
        }
    }

    private fun parseCondition(value: Any?): Condition {
        if (value == null) {
            return Conditions.create(clazz, Configuration.empty())
        }
        require(value is ConfigurationSection) { "Filter must be a configuration section." }
        return Conditions.create(clazz, value)
    }

    private fun parseRule(value: Any?): Rule<Event> {
        if (value == null) {
            return Rules.create(clazz, Configuration.empty())
        }
        require(value is ConfigurationSection) { "Rule must be a configuration section." }
        return Rules.create(clazz, value)
    }

    private fun parsePlayerField(): Field? {
        if (!playerRequired || playerReference != "@") {
            return null
        }
        // 自动识别玩家字段
        val fields = mutableListOf<Field>()
        for (field in clazz.declaredFields) {
            if (Entity::class.java.isAssignableFrom(field.type)) {
                fields += field
            }
        }
        require(playerRequired && fields.isNotEmpty()) { "Player field not found." }
        require(playerRequired && fields.size == 1) { "Multiple player fields found." }
        fields[0].isAccessible = true
        return fields[0]
    }

    private fun parseCooldown(value: Any?): Long {
        if (value == null) {
            return -1L
        }
        return when (value) {
            is Int -> value.toLong() * 50L
            is Long -> value * 50L
            is String -> {
                val regex = Regex("^(\\d+)(ticks|tick|t|ms|s|m|h|d)$", RegexOption.IGNORE_CASE)
                val matches = regex.find(value) ?: error("Invalid time format: $value")
                val time = matches.groupValues[1].toLong()
                when (val unit = matches.groupValues[2].lowercase()) {
                    "ticks", "tick", "t" -> time * 50L
                    "ms" -> time
                    "s" -> time * 1_000
                    "m" -> time * 60_000
                    "h" -> time * 3_600_000
                    "d" -> time * 86_400_000
                    else -> error("Invalid time unit: $unit")
                }
            }
            else -> error("Invalid time format: $value")
        }
    }

    private fun parseEventPriority(value: String): EventPriority {
        val name = value.uppercase()
        require(name in EventPriority.entries.map(EventPriority::name)) { "Priority must be one of ${EventPriority.entries.map(EventPriority::name)}" }
        return EventPriority.valueOf(name)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseEventClass(value: String?): Class<out Event> {
        require(!value.isNullOrBlank()) { "Event class cannot be null or blank." }
        val clazz = Class.forName(value)
        require(Event::class.java.isAssignableFrom(clazz)) { "Event class must be subclass of class ${Event::class.java.name}" }
        return clazz as Class<out Event>
    }

}