package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.adaptPlayer
import taboolib.common.platform.function.console
import top.lanscarlos.vulpecula.common.applicative.Applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:30
 */
data class Context(val event: Event) {

    var isCancelled: Boolean = false

    var isFiltered: Boolean = false

    var player: Player? = null
        private set

    private var isPlayerInitialized: Boolean = false

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

    /**
     * @param force 是否强制替换玩家变量
     * */
    fun setPlayer(player: Player?, force: Boolean = false) {
        if (!force && isPlayerInitialized) {
            return
        }
        this.player = player
        if (player != null) {
            setVariable("player", player)
        } else {
            removeVariable("player")
        }
    }

    fun sender(): ProxyCommandSender {
        return player?.let(::adaptPlayer) ?: console()
    }

    fun variables(): Map<String, Any> {
        return variables
    }

    fun getVariableOrNull(key: String): Any? {
        return variables[key]
    }

    fun <T> getVariableOrNull(key: String, applicative: Applicative<T>): T? {
        return getVariableOrNull(key)?.let(applicative::convert)
    }

    fun getVariable(key: String): Any {
        return variables[key]!!
    }

    fun <T> getVariable(key: String, applicative: Applicative<T>): T {
        return applicative.convert(getVariable(key))
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
