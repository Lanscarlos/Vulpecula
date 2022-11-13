package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.kether.live.BooleanLiveData
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:45
 */
object ItemPotionHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("potion")

    override fun read(isRoot: Boolean, reader: QuestReader): ActionItemStack.Handler {
        val source = if (isRoot) reader.readItemStack() else null
        reader.mark()
        return when (reader.nextToken()) {
            "insert", "add",
            "modify", "set" -> modify(reader, source)
            "remove", "rm" -> remove(reader, source)
            "clear" -> clear(source)
            "contains", "contain", "has" -> contains(reader, source)
            "base" -> base(reader, source)
            "color" -> color(reader, source)
            else -> {
                reader.reset()
                acceptHandler(source) { item -> item.itemMeta?.lore }
            }
        }
    }

    fun modify(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = StringLiveData(reader.nextBlock())
        val options = mutableMapOf<String, LiveData<*>>()
        if (!reader.nextPeek().startsWith('-')) {
            options["duration"] = reader.readDouble()
            options["amplifier"] = reader.readDouble()
        }
        while (reader.nextPeek().startsWith('-')) {
            when (val it = reader.nextToken().substring(1)) {
                "duration", "dura", "time" -> options["duration"] = reader.readInt()
                "amplifier", "amp", "level", "lvl" -> options["amplifier"] = reader.readInt()
                "ambient", "amb" -> options["ambient"] = reader.readBoolean()
                "particles", "particle", "p" -> options["particles"] = reader.readBoolean()
                "icon", "i" ->  options["icon"] = reader.readBoolean()
                else -> error("Unknown argument \"$it\" at item potion action.")
            }
        }

        return applyTransfer(source) { _, itemMeta ->
            val meta = itemMeta as? PotionMeta ?: return@applyTransfer itemMeta

            val potion = PotionEffect(
                type.asPotionEffectType(this) ?: PotionEffectType.SLOW,
                options["duration"].getValue(this, 200),
                options["amplifier"].getValue(this, 0) - 1,
                options["ambient"].getValue(this, false),
                options["particles"].getValue(this, true),
                options["icon"].getValue(this, true)
            )

            meta.also { it.addCustomEffect(potion, true) }
        }
    }

    fun remove(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val raw = StringLiveData(reader.nextBlock())

        return applyTransfer(source) { _, itemMeta ->
            val meta = itemMeta as? PotionMeta ?: return@applyTransfer itemMeta
            val type = raw.asPotionEffectType(this) ?: return@applyTransfer meta

            meta.also { it.removeCustomEffect(type) }
        }
    }

    fun clear(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return applyTransfer(source) { _, itemMeta ->
            val meta = itemMeta as? PotionMeta ?: return@applyTransfer itemMeta
            meta.also { it.clearCustomEffects() }
        }
    }

    fun contains(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val raw = if (!reader.hasNextToken("any")) StringLiveData(reader.nextBlock()) else null

        return acceptHandler(source) { item ->
            val meta = item.itemMeta as? PotionMeta ?: return@acceptHandler false

            if (raw == null) return@acceptHandler meta.hasCustomEffects()
            val type = raw.asPotionEffectType(this) ?: return@acceptHandler false

            meta.hasCustomEffect(type)
        }
    }

    private fun base(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        reader.mark()
        return when (val it = reader.nextToken()) {
            "type", "extended", "upgraded" -> {
                acceptHandler(source) { item ->
                    val meta = item.itemMeta as? PotionMeta ?: return@acceptHandler null
                    when (it) {
                        "type" -> meta.basePotionData.type
                        "extended" -> meta.basePotionData.isExtended
                        "upgraded" -> meta.basePotionData.isUpgraded
                        else -> meta.basePotionData
                    }
                }
            }
            "to" -> {
                val type = StringLiveData(reader.nextBlock())
                val extend = if (reader.hasNextToken("with", "by")) {
                    reader.readBoolean() to reader.readBoolean()
                } else {
                    BooleanLiveData(false) to BooleanLiveData(false)
                }

                applyTransfer(source) { _, itemMeta ->
                    val meta = itemMeta as? PotionMeta ?: return@applyTransfer itemMeta

                    val potionData = PotionData(
                        type.asPotionType(this) ?: PotionType.SLOWNESS,
                        extend.first.get(this, false),
                        extend.second.get(this, false)
                    )

                    meta.also { meta.basePotionData = potionData }
                }
            }
            else -> {
                acceptHandler(source) { item ->
                    (item.itemMeta as? PotionMeta)?.basePotionData
                }
            }
        }
    }

    private fun color(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val color = reader.readColor()

        return applyTransfer(source) { _, itemMeta ->
            val meta = itemMeta as? PotionMeta ?: return@applyTransfer itemMeta

            meta.also { it.color = color.get(this, Color.WHITE).toBukkit() }
        }
    }

    private fun LiveData<String>.asPotionEffectType(frame: ScriptFrame): PotionEffectType? {
        val name = this.getOrNull(frame) ?: return null
        return PotionEffectType.values().firstOrNull { name.equals(it.name, true) }
    }

    private fun LiveData<String>.asPotionType(frame: ScriptFrame): PotionType? {
        val name = this.getOrNull(frame) ?: return null
        return PotionType.values().firstOrNull { name.equals(it.name, true) }
    }

    private fun Color.toBukkit(): org.bukkit.Color {
        return org.bukkit.Color.fromRGB(this.rgb)
    }
}