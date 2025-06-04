package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * 泛用
 *
 * @author Lanscarlos
 * @since 2025/6/4 13:38
 */
class GenericEventRule(clazz: ReflexClass, config: ConfigurationSection) : AbstractRule<Event>(clazz, config)