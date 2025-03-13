package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 14:14
 */
abstract class AbstractRule<T: Event>(val clazz: Class<out Event>, val config: ConfigurationSection) : Rule<T>