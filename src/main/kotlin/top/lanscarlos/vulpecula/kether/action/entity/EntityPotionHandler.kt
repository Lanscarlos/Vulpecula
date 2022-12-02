package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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
object EntityPotionHandler : ActionEntity.Reader {

    override val name: Array<String> = arrayOf("potion")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionEntity.Handler {
        val source = reader.source(isRoot)
        reader.mark()
        return when (reader.nextToken()) {
            "add", "set" -> addPotion(reader, source)
            "remove", "rm" -> removePotion(reader, source)
            "clear" -> clearPotion(source)
            "contains", "contain", "has" -> containsPotion(reader, source)
            else -> {
                reader.reset()
                addPotion(reader, source)
            }
        }
    }

    private fun addPotion(reader: QuestReader, source: LiveData<Entity>?): ActionEntity.Handler {
        val type = reader.readString()
        val options = mutableMapOf<String, LiveData<*>>()
        if (!reader.nextPeek().startsWith('-')) {
            options["duration"] = reader.readInt()
            options["amplifier"] = reader.readInt()
        }
        while (reader.nextPeek().startsWith('-')) {
            when (val it = reader.nextToken().substring(1)) {
                "duration", "dura", "time" -> options["duration"] = reader.readInt()
                "amplifier", "amp", "level", "lvl" -> options["amplifier"] = reader.readInt()
                "ambient", "amb" -> options["ambient"] = reader.readBoolean()
                "particles", "particle", "p" -> options["particles"] = reader.readBoolean()
                "icon", "i" ->  options["icon"] = reader.readBoolean()
                else -> error("Unknown argument \"$it\" at entity potion action.")
            }
        }

        return acceptTransferFuture(source) { entity ->
            listOf(
                type.getOrNull(this),
                options["duration"]?.getOrNull(this),
                options["amplifier"]?.getOrNull(this),
                options["ambient"]?.getOrNull(this),
                options["particles"]?.getOrNull(this),
                options["icon"]?.getOrNull(this)
            ).thenTake().thenApply {
                val potion = PotionEffect(
                    it[0]?.toString()?.asPotionEffectType() ?: PotionEffectType.SLOW,
                    it[1].coerceInt(200),
                    it[2].coerceInt(1) - 1,
                    it[3].coerceBoolean(false),
                    it[4].coerceBoolean(true),
                    it[5].coerceBoolean(true)
                )

                (entity as? LivingEntity)?.addPotionEffect(potion)
                return@thenApply entity
            }
        }
    }

    private fun removePotion(reader: QuestReader, source: LiveData<Entity>?): ActionEntity.Handler {
        val type = reader.readString()

        return acceptTransferFuture(source) { entity ->
            type.getOrNull(this).thenApply { name ->
                name?.asPotionEffectType()?.let {
                    (entity as? LivingEntity)?.removePotionEffect(it)
                }
                return@thenApply entity
            }
        }
    }

    private fun clearPotion(source: LiveData<Entity>?): ActionEntity.Handler {
        return acceptTransferNow(source) { entity ->
            (entity as? LivingEntity)?.activePotionEffects?.forEach { entity.removePotionEffect(it.type) }
            return@acceptTransferNow entity
        }
    }

    private fun containsPotion(reader: QuestReader, source: LiveData<Entity>?): ActionEntity.Handler {
        val type = reader.readString()

        return acceptHandleFuture(source) { entity ->
            type.getOrNull(this).thenApply { name ->
                return@thenApply name?.asPotionEffectType()?.let {
                    (entity as? LivingEntity)?.hasPotionEffect(it)
                } ?: false
            }
        }
    }

    private fun String.asPotionEffectType(): PotionEffectType? {
        return PotionEffectType.values().firstOrNull { it.name.equals(this, true) }
    }
}