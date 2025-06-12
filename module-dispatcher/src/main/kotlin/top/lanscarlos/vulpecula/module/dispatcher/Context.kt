package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.entity.Player
import org.bukkit.event.Event

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:30
 */
data class Context(val event: Event) {

    var player: Player? = null

    var isCancelled: Boolean = false

    var isFiltered: Boolean = false

    val variables: HashMap<String, Any> = hashMapOf()

    fun setVariable(key: String, value: Any) {
        variables[key] = value
    }

    fun setVariables(vararg keys: String, value: Any) {
        for (key in keys) {
            variables[key] = value
        }
    }

}
