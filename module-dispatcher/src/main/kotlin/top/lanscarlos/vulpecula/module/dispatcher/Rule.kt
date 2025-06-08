package top.lanscarlos.vulpecula.module.dispatcher

import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.dispatcher.rule.RuleRegistry

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher
 *
 * @author Lanscarlos
 * @since 2025/6/4 11:24
 */
interface Rule<T: Event> {

    /**
     * 从事件中解析出玩家主体
     * */
    fun parsePlayer(event: T): Player?

    /**
     * 从事件中解析出事件变量
     * */
    fun parseVariables(event: T): Map<String, Any?>

    /**
     * 判断此次事件是否通过规则
     * */
    fun matches(context: Context): Boolean

    /**
     * 更新阻断器
     * */
    fun updateBaffle(context: Context)

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <T: Event> of(clazz: ReflexClass, section: ConfigurationSection): Rule<T> {
            return RuleRegistry.get(clazz).newInstance(clazz, section) as Rule<T>
        }

    }

}