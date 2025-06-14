package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.entity.Player
import org.bukkit.event.Event
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.config.boolean
import top.lanscarlos.vulpecula.common.config.convert
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.module.dispatcher.Context

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * 反射玩家处理流, 用于反射获取玩家对象
 *
 * @author Lanscarlos
 * @since 2025/6/12 10:11
 */
@AutoRegistered
class ReflexPlayerPipeline(clazz: Class<*>, config: ConfigurationSection) : AbstractPipeline<Event>(clazz, config) {

    override val priority: Int = 0 // 反射性能消耗大, 通常置于最后做兜底处理

    val playerRequired: Boolean by config.read("player-required").boolean(false)

    val playerField: ClassField? by config.read("player-field").string("~").convert(::parsePlayerField)

    override fun initPlayer(context: Context) {
        // 解析玩家对象
        context.setPlayer(playerField?.get(context.event) as? Player)
    }

    override fun filter(context: Context) {
        if (playerRequired && context.player == null) {
            // 玩家不存在, 过滤本次事件
            context.filter()
        }
    }

    private fun parsePlayerField(value: String): ClassField? {
        val clazz = ReflexClass.of(this.clazz)
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

}