package top.lanscarlos.vulpecula.bacikal.action.target.selector

import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.ProxyPlayer
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.action.target.ActionTarget
import top.lanscarlos.vulpecula.utils.playerOrNull
import top.lanscarlos.vulpecula.utils.toBukkit

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.target.selector
 *
 * @author Lanscarlos
 * @since 2023-03-23 09:10
 */
object ActionTargetSelectWorld : ActionTarget.Resolver {

    private enum class Type(vararg namespace: String) {
        EntitiesInWorld("EntitiesInWorld", "EntityInWorld", "EIW"),
        LivingEntitiesInWorld("LivingEntitiesInWorld", "LivingEntityInWorld", "LEIW"),
        PlayersInWorld("PlayersInWorld", "PlayerInWorld", "PIW"),
        AnimalsInWorld("AnimalsInWorld", "AnimalInWorld", "AIW");

        val namespace = namespace.map { it.lowercase() }
    }

    override val name: Array<String> = Type.values().flatMap { it.namespace }.toTypedArray()

    override fun resolve(reader: ActionTarget.Reader): ActionTarget.Handler<out Any?> {
        val type = Type.values().first { reader.token in it.namespace }

        return reader.transfer {
            combine(
                source(),
                optional("at", then = any()),
                argument("self", then = LiveData.point(true), def = false)
            ) { target, any, self ->

                // 解析 world
                val world = when (any) {
                    is World -> any
                    is String -> Bukkit.getWorld(any)
                    is taboolib.common.util.Location -> any.world?.let { Bukkit.getWorld(it) }
                    is org.bukkit.Location -> any.world
                    is Entity -> any.world
                    is Block -> any.world
                    is ProxyPlayer -> Bukkit.getWorld(any.world)
                    else -> this.playerOrNull()?.world?.let { Bukkit.getWorld(it) }
                } ?: error("No world selected.")

                // 获取实体集合
                val entities = when (type) {
                    Type.PlayersInWorld -> world.players
                    Type.EntitiesInWorld -> world.entities
                    Type.LivingEntitiesInWorld -> world.entities.filterIsInstance<LivingEntity>()
                    Type.AnimalsInWorld -> world.entities.filterIsInstance<Animals>()
                }

                // 加入目标集合
                target.add(entities)

                // 排除自己
                if (!self) {
                    this.playerOrNull()?.toBukkit()?.let {
                        target -= it
                    }
                }

                target
            }
        }
    }
}