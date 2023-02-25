package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common5.cbool
import taboolib.library.kether.Parser
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readBoolean
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.utils.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:51
 */
object EntityPotionHandler : ActionEntity.Resolver {

    override val name: Array<String> = arrayOf("potion")

    override fun resolve(reader: ActionEntity.Reader): ActionEntity.Handler<out Any?> {
        val source = reader.source()

        reader.mark()
        return when (reader.nextToken()) {
            "add", "set" -> {
                addPotion(reader, source)
            }
            "remove", "rm" -> {
                reader.transfer {
                    group(
                        source,
                        string()
                    ) { entity, type ->
                        if (entity !is LivingEntity) {
                            warning("Cannot remove potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                            return@group now { entity }
                        }

                        type.asPotionEffectType()?.let {
                            entity.removePotionEffect(it)
                        } ?: warning("Unknown potion type: $type [ERROR: entity@${reader.token}]")
                        return@group now { entity }
                    }
                }
            }
            "clear" -> {
                reader.transfer {
                    group(
                        source
                    ) { entity ->
                        if (entity !is LivingEntity) {
                            warning("Cannot clear potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                            return@group now { entity }
                        }

                        entity.activePotionEffects.forEach { entity.removePotionEffect(it.type) }
                        return@group now { entity }
                    }
                }
            }
            "contains", "contain", "has" -> {
                reader.handle {
                    group(
                        source,
                        string()
                    ) { entity, type ->
                        if (entity !is LivingEntity) {
                            warning("Cannot check potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                            return@group now { entity }
                        }

                        return@group now {
                            type.asPotionEffectType()?.let {
                                entity.hasPotionEffect(it)
                            } ?: false
                        }
                    }
                }
            }
            else -> {
                reader.reset()
                addPotion(reader, source)
            }
        }
    }

    private fun addPotion(reader: ActionEntity.Reader, source: Parser<Entity>): ActionEntity.Handler<out Any?> {
        return reader.transfer {
            group(
                source,
                string("SLOW"),
                int(200),
                int(1),
                arguments(
                    arrayOf("ambient", "amb") to boolean(false),
                    arrayOf("particles", "particle", "p") to boolean(true),
                    arrayOf("icon", "i") to boolean(true),
                    prefix = "-"
                )
            ) { entity, type, duration, level, options ->
                if (entity !is LivingEntity) {
                    warning("Cannot remove potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                    return@group now { entity }
                }

                info("option -> $options")
                val potion = PotionEffect(
                    type.asPotionEffectType() ?: PotionEffectType.SLOW,
                    duration,
                    level - 1,
                    options["ambient"]?.cbool ?: false,
                    options["particles"]?.cbool ?: true,
                    options["icon"]?.cbool ?: true,
                )

                entity.addPotionEffect(potion)
                return@group now { entity }
            }
        }
    }

    private fun String.asPotionEffectType(): PotionEffectType? {
        return PotionEffectType.values().firstOrNull { it.name.equals(this, true) }
    }
}