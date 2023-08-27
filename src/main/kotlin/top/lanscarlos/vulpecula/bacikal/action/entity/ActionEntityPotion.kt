package top.lanscarlos.vulpecula.bacikal.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.function.warning
import top.lanscarlos.vulpecula.bacikal.LiveData

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.entity
 *
 * @author Lanscarlos
 * @since 2022-11-17 23:51
 */
object ActionEntityPotion : ActionEntity.Resolver {

    override val name: Array<String> = arrayOf("potion")

    /**
     * entity potion &entity add &type &time &level
     * entity potion &entity remove &type
     * entity potion &entity clear
     * entity potion &entity has &type
     * entity potion &entity &type &time &level
     * */
    override fun resolve(reader: ActionEntity.Reader): ActionEntity.Handler<out Any?> {
        val source = reader.source().accept(reader)

        reader.mark()
        return when (reader.nextToken()) {
            "add", "set" -> {
                addPotion(reader, source)
            }
            "remove", "rm" -> {
                reader.transfer {
                    combine(
                        source,
                        text(display = "potion type")
                    ) { entity, type ->
                        if (entity !is LivingEntity) {
                            warning("Cannot check potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                            return@combine entity
                        }

                        type.asPotionEffectType()?.let {
                            entity.removePotionEffect(it)
                        } ?: warning("Unknown potion type: $type [ERROR: entity@${reader.token}]")

                        return@combine entity
                    }
                }
            }
            "clear" -> {
                reader.transfer {
                    combine(
                        source
                    ) { entity ->
                        if (entity !is LivingEntity) {
                            warning("Cannot check potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                            return@combine entity
                        }
                        entity.activePotionEffects.forEach { entity.removePotionEffect(it.type) }
                        return@combine entity
                    }
                }
            }
            "contains", "contain", "has" -> {
                reader.handle {
                    combine(
                        source,
                        text(display = "potion type")
                    ) { entity, type ->
                        if (entity !is LivingEntity) {
                            warning("Cannot check potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                            return@combine entity
                        }
                        return@combine type.asPotionEffectType()?.let { entity.hasPotionEffect(it) } ?: false
                    }
                }
            }
            else -> {
                reader.reset()
                addPotion(reader, source)
            }
        }
    }

    private fun addPotion(reader: ActionEntity.Reader, source: LiveData<Entity>): ActionEntity.Handler<out Any?> {
        return reader.transfer {
            combine(
                source,
                text(display = "potion type"),
                int(200, display = "duration"),
                int(1, display = "level"),
                argument("ambient", "amb", then = bool(false), def = false),
                argument("particles", "particle", "p", then = bool(true), def = true),
                argument("icon", "i", then = bool(true), def = true)
            ) { entity, type, duration, level, ambient, particles, icon ->
                if (entity !is LivingEntity) {
                    warning("Cannot remove potion from this type of entity: ${entity.type.name} [ERROR: entity@${reader.token}]")
                    return@combine entity
                }

                val potion = try {
                    // Minecraft 1.13+
                    PotionEffect(
                        type.asPotionEffectType() ?: error("No potion type \"$type\" found."),
                        duration,
                        level - 1,
                        ambient,
                        particles,
                        icon
                    )
                } catch (ex: NoSuchMethodError) {
                    // Minecraft 1.12.2+
                    PotionEffect(
                        type.asPotionEffectType() ?: error("No potion type \"$type\" found."),
                        duration,
                        level - 1,
                        ambient,
                        particles
                    )
                }

                entity.also { it.addPotionEffect(potion) }
            }
        }
    }

    private fun String.asPotionEffectType(): PotionEffectType? {
        return PotionEffectType.getByName(this)
    }
}