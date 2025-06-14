package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:30
 */
data class Context(val event: Event) {

    private var isPlayerInitialized: Boolean = false

    var player: Player? = null

    var isCancelled: Boolean = false
        private set

    var isFiltered: Boolean = false
        private set

    private val variables: HashMap<String, Any> = hashMapOf()

    /**
     * 过滤事件
     * */
    fun filter() {
        isFiltered = true
    }

    /**
     * 取消事件
     * */
    fun cancel() {
        isCancelled = true
    }

    fun setPlayer(player: Player?, force: Boolean = false) {
        if (!force && isPlayerInitialized) {
            return
        }
        this.player = player
    }

    fun sender(): ProxyCommandSender {
        return player?.let(::adaptPlayer) ?: console()
    }

    fun variables(): Map<String, Any> {
        return variables
    }

    fun setVariable(key: String, value: Any?) {
        if (value == null) {
            return
        }
        variables[key] = value
    }

    fun setVariables(vararg keys: String, value: Any?) {
        if (value == null) {
            return
        }
        for (key in keys) {
            variables[key] = value
        }
    }

    fun removeVariable(vararg keys: String) {
        for (key in keys) {
            variables.remove(key)
        }
    }

}
