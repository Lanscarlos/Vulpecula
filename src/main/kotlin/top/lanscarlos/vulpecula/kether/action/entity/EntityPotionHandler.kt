package top.lanscarlos.vulpecula.kether.action.entity

import org.bukkit.entity.Entity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.kether.live.LiveData
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

        return applyLivingEntity(source) { entity ->
            val potion = PotionEffect(
                type.asPotionEffectType(this) ?: PotionEffectType.SLOW,
                options["duration"].getValue(this, 200),
                options["amplifier"].getValue(this, 0) - 1,
                options["ambient"].getValue(this, false),
                options["particles"].getValue(this, true),
                options["icon"].getValue(this, true)
            )

            entity.also { it.addPotionEffect(potion) }
        }
    }

    private fun removePotion(reader: QuestReader, source: LiveData<Entity>?): ActionEntity.Handler {
        val type = reader.readString()

        return applyLivingEntity(source) { entity ->
            type.asPotionEffectType(this)?.let { entity.removePotionEffect(it) }
            entity
        }
    }

    private fun containsPotion(reader: QuestReader, source: LiveData<Entity>?): ActionEntity.Handler {
        val type = reader.readString()

        return acceptLivingEntity(source) { entity ->
            type.asPotionEffectType(this)?.let { entity.hasPotionEffect(it) } ?: false
        }
    }

    private fun LiveData<String>.asPotionEffectType(frame: ScriptFrame): PotionEffectType? {
        val name = this.getOrNull(frame) ?: return null
        return PotionEffectType.values().firstOrNull { name.equals(it.name, true) }
    }
}