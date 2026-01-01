package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.block.Block
import org.bukkit.entity.Entity
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
data class PipelineContext(val event: Event) {

    /**
     * 事件主体
     * */
    var principal: Any? = Unit
        private set

    /**
     * 事件主体唯一标识
     * */
    var principalId: String = "NULL"
        private set

    var isCancelled: Boolean = false
        private set

    var isFiltered: Boolean = false
        private set

    var isFilterBaffled: Boolean = false
        private set

    var player: Player? = null
        private set

    var result: Any? = null

    var isPrincipalInitialized: Boolean = false
        private set

    private val variables: HashMap<String, Any> = hashMapOf("@VULPECULA_CONTEXT_EVENT" to event)

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
     * 阻断过滤
     * */
    fun baffleFilter() {
        isFilterBaffled = true
    }

    /**
     * 获取事件主体
     *
     * @param replace 是否替换已存在的事件主体
     * */
    fun setPrincipal(principal: Any?, replace: Boolean = false) {
        if (!replace && isPrincipalInitialized) {
            return
        }
        this.principal = principal
        isPrincipalInitialized = true

        // 处理标识
        principalId = when (principal) {
            is Unit -> "NULL"
            is Player -> "PLAYER@${principal.uniqueId}"
            is Entity -> "ENTITY@${principal.uniqueId}"
            is Block -> "BLOCK@${principal.world.name},${principal.x},${principal.y},${principal.z}"
            else -> error("Unsupported type: ${principal?.javaClass?.canonicalName ?: "NULL"}")
        }

        // 处理玩家对象
        player = principal as? Player
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