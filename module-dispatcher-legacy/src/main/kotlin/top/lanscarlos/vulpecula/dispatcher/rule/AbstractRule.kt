package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.dispatcher.Context
import java.lang.reflect.Field

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025-03-12 14:14
 */
abstract class AbstractRule<T: Event>(val clazz: Class<out Event>, val config: ConfigurationSection) : Rule<T> {

    val playerRequired: Boolean by config.read("player-required").boolean(true)

    val playerReference: String by config.read("player-reference").string("@")

    val playerField: ClassField? by lazy(::parsePlayerField)

    override fun parsePlayer(context: Context) {
        context.player = playerField?.get(context.event()) as? Player
    }

    private fun parsePlayerField(): ClassField? {
        val reflex = ReflexClass.of(clazz)
        if (playerReference != "@") {
            return reflex.getField(playerReference)
        }

        // 自动检索玩家字段
        val fields = mutableListOf<Field>()
        for (field in clazz.declaredFields) {
            if (Player::class.java.isAssignableFrom(field.type) || field.type.isAssignableFrom(Player::class.java)) {
                fields += field
            }
        }
        if (fields.size == 1) {
            return reflex.getField(fields[0].name)
        }
        if (playerRequired) {
            require(fields.isNotEmpty()) { "Player field not found." }
            require(fields.size == 1) { "Multiple player fields found." }
        }
        return null
    }

}