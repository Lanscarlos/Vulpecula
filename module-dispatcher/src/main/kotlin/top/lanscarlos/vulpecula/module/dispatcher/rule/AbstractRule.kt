package top.lanscarlos.vulpecula.module.dispatcher.rule

import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.common5.Baffle
import taboolib.common5.Baffle.BaffleCounter
import taboolib.common5.Baffle.BaffleTime
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.core.exception.InvalidTypeException
import top.lanscarlos.vulpecula.module.dispatcher.Context
import top.lanscarlos.vulpecula.module.dispatcher.DispatcherRule
import top.lanscarlos.vulpecula.common.core.utils.TimeUtil
import java.util.concurrent.TimeUnit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/4 12:00
 */
abstract class AbstractRule<T: Event>(val clazz: ReflexClass, val config: ConfigurationSection) : DispatcherRule<T> {

    open val baffle: Baffle? by config.read("baffle").convert(::parseBaffle)

    open val playerRequired: Boolean by config.read("player-required").boolean(false)

    open val playerField: ClassField? by config.read("player-field").string("~").convert(::parsePlayerField)

    override fun parsePlayer(event: T): Player? {
        return playerField?.get(event) as? Player
    }

    override fun parseVariables(event: T): Map<String, Any?> {
        return mapOf("eventName" to event.eventName)
    }

    override fun matches(context: Context): Boolean {
        if (baffle != null && !baffle!!.hasNext("*", false)) {
            // 冷却
            return false
        }
        if (playerRequired && context.player == null) {
            // 无玩家对象
            return false
        }
        return true
    }

    override fun updateBaffle(context: Context) {
        val baffle = baffle ?: return
        baffle.next()
    }

    private fun parsePlayerField(value: String): ClassField? {
        if (value != "~") {
            return clazz.getField(value)
        }

        // 自动检索玩家字段
        val fields = mutableListOf<ClassField>()
        for (field in clazz.structure.fields) {
            val type = field.fieldType
            if (Player::class.java.isAssignableFrom(type) || type.isAssignableFrom(Player::class.java)) {
                fields += field
            }
        }
        if (fields.size == 1) {
            return fields[0]
        }
        if (playerRequired) {
            require(fields.isNotEmpty()) { "Player field not found." }
            require(fields.size == 1) { "Multiple player fields found." }
        }
        return null
    }

    private fun parseBaffle(value: Any?): Baffle? {
        if (value == null) {
            return null
        }
        return when (value) {
            is Number -> BaffleCounter.of(value.toInt())
            is String -> BaffleTime.of(TimeUtil.parse(value), TimeUnit.MILLISECONDS)
            else -> throw InvalidTypeException(value)
        }
    }

}