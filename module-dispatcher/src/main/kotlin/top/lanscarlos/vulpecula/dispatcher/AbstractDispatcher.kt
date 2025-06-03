package top.lanscarlos.vulpecula.dispatcher

import org.bukkit.event.Event
import taboolib.common.platform.event.EventPriority
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.int
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.dispatcher.condition.Condition
import top.lanscarlos.vulpecula.dispatcher.condition.Conditions
import top.lanscarlos.vulpecula.dispatcher.rule.Rule
import top.lanscarlos.vulpecula.dispatcher.rule.Rules

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/3 13:20
 */
abstract class AbstractDispatcher(override val id: String, val config: Configuration) : Dispatcher {

    override val clazz: Class<out Event> by config.read("listen-event").string().convert(::parseEventClass)

    override val priority: EventPriority by config.read("listen-priority").string("NORMAL").convert(::parseEventPriority)

    override val weight: Int by config.read("weight").int(8)

    val rule: Rule<Event> by config.read("rule").convert(::parseRule)

    val filter: Condition by config.read("filter").convert(::parseCondition)

    val baffle: Condition by config.read("baffle").convert(::parseCondition)

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

    private fun parseEventPriority(value: String): EventPriority {
        val name = value.uppercase()
        require(name in EventPriority.entries.map(EventPriority::name)) { "Priority must be one of ${EventPriority.entries.map(EventPriority::name)}" }
        return EventPriority.valueOf(name)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseEventClass(value: String): Class<out Event> {
        require(value.isNotBlank()) { "Event class cannot be null or blank." }
        val clazz = try {
            Class.forName(value)
        } catch (e: ClassNotFoundException) {
            error("Event class not found: $value")
        }
        require(Event::class.java.isAssignableFrom(clazz)) { "Event class must be subclass of class ${Event::class.java.name}" }
        return clazz as Class<out Event>
    }

}