package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event
import top.lanscarlos.vulpecula.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 09:03
 */
interface Rule<T: Event> {

    /**
     * 解析玩家
     * */
    fun parsePlayer(context: Context)

    /**
     * 匹配事件
     * */
    fun matches(context: Context): Boolean

}