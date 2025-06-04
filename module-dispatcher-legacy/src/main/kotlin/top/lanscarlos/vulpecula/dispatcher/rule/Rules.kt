package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event
import org.bukkit.event.player.PlayerMoveEvent
import taboolib.library.configuration.ConfigurationSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 16:34
 */
object Rules {

    val registry: HashMap<Class<out Event>, Class<out Rule<out Event>>> = hashMapOf()

    init {
        registry[PlayerMoveEvent::class.java] = PlayerMoveEventRule::class.java
    }

    @Suppress("UNCHECKED_CAST")
    fun create(event: Class<out Event>, config: ConfigurationSection): Rule<Event> {
        val clazz = registry[event] ?: return GenericEventRule(event, config)
        val constructor = clazz.getDeclaredConstructor(Class::class.java, ConfigurationSection::class.java)
        return constructor.newInstance(event, config) as Rule<Event>
    }

}