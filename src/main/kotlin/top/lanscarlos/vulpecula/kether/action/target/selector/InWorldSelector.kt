package top.lanscarlos.vulpecula.kether.action.target.selector

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import taboolib.platform.util.toBukkitLocation
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.target.selector
 *
 * @author Lanscarlos
 * @since 2022-11-17 21:26
 */
object InWorldSelector : ActionTarget.Reader {

    enum class Type(vararg namespace: String) {
        EntitiesInWorld("entities-in-world", "entity-in-world", "EntitiesInWorld", "EntityInWorld", "EIW"),
        LivingEntitiesInWorld("living-entities-in-world", "living-entity-in-world", "LivingEntitiesInWorld", "LivingEntityInWorld", "LEIW"),
        PlayersInWorld("players-in-world", "player-in-world", "PlayersInWorld", "PlayerInWorld", "PIW"),
        AnimalsInWorld("animals-in-world", "animal-in-world", "AnimalsInWorld", "AnimalInWorld");

        val namespace = namespace.map { it.lowercase() }
    }

    override val name: Array<String> = Type.values().flatMap { it.namespace.toList() }.toTypedArray()

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionTarget.Handler {
        val type = Type.values().firstOrNull { input.lowercase() in it.namespace }

        if (reader.hasNextToken("at")) {
            val raw = reader.nextBlock()
            val includeSelf = reader.hasNextToken("-self")

            return handleFuture { collection ->
                // 解析 world
                this.run(raw).thenApply {
                    val world = when (it) {
                        is World -> it
                        is String -> Bukkit.getWorld(it)
                        is Location -> it.world
                        is taboolib.common.util.Location -> it.toBukkitLocation().world
                        is Entity -> it.world
                        is ProxyPlayer -> it.location.toBukkitLocation().world
                        else -> null
                    } ?: this.playerOrNull()?.location?.toBukkitLocation()?.world ?: error("No world selected.")

                    return@thenApply collection.process(this, type, world, includeSelf)
                }
            }
        } else {
            val includeSelf = reader.hasNextToken("-self")

            return handleNow { collection ->
                val world = this.playerOrNull()?.location?.toBukkitLocation()?.world ?: error("No world selected.")

                return@handleNow collection.process(this, type, world, includeSelf)
            }
        }
    }

    private fun MutableCollection<Any>.process(frame: ScriptFrame, type: Type?, world: World, includeSelf: Boolean): MutableCollection<Any> {
        when (type) {
            Type.PlayersInWorld -> world.players
            Type.EntitiesInWorld -> world.entities
            Type.LivingEntitiesInWorld -> world.entities.filterIsInstance<LivingEntity>()
            Type.AnimalsInWorld -> world.entities.filterIsInstance<Animals>()
            else -> null
        }?.let { element ->
            this.addAll(element)
        }

        // 排除自己
        if (!includeSelf) {
            frame.playerOrNull()?.toBukkit()?.let { self ->
                this.remove(self)
            }
        }

        return this
    }
}