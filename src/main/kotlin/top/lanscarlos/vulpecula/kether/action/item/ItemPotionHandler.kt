package top.lanscarlos.vulpecula.kether.action.item

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.kether.live.*
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.item
 *
 * @author Lanscarlos
 * @since 2022-11-13 20:45
 */
@Deprecated("")
object ItemPotionHandler : ActionItemStack.Reader {

    override val name: Array<String> = arrayOf("potion")

    override fun read(reader: QuestReader, input: String, isRoot: Boolean): ActionItemStack.Handler {
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
                acceptHandleNow(source) { item -> item.itemMeta?.lore }
            }
        }
    }

    fun modify(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val type = StringLiveData(reader.nextBlock())
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
                else -> error("Unknown argument \"$it\" at item potion action.")
            }
        }

        return acceptTransferFuture(source) { item ->
            listOf(
                type.getOrNull(this),
                options["duration"]?.getOrNull(this),
                options["amplifier"]?.getOrNull(this),
                options["ambient"]?.getOrNull(this),
                options["particles"]?.getOrNull(this),
                options["icon"]?.getOrNull(this)
            ).thenTake().thenApply { args ->
                val meta = item.itemMeta as? PotionMeta ?: return@thenApply item
                val potion = PotionEffect(
                    args[0]?.toString()?.asPotionEffectType() ?: PotionEffectType.SLOW,
                    args[1].coerceInt(200),
                    args[2].coerceInt(1) - 1,
                    args[3].coerceBoolean(false),
                    args[4].coerceBoolean(true),
                    args[5].coerceBoolean(true)
                )
                meta.addCustomEffect(potion, true)

                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    fun remove(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val raw = StringLiveData(reader.nextBlock())

        return acceptTransferFuture(source) { item ->
            raw.getOrNull(this).thenApply { name ->
                val meta = item.itemMeta as? PotionMeta ?: return@thenApply item
                val type = name?.asPotionEffectType() ?: return@thenApply item

                meta.removeCustomEffect(type)
                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    fun clear(source: LiveData<ItemStack>?): ActionItemStack.Handler {
        return acceptTransferNow(source) { item ->
            val meta = item.itemMeta as? PotionMeta ?: return@acceptTransferNow item

            meta.clearCustomEffects()
            return@acceptTransferNow item.also { it.itemMeta = meta }
        }
    }

    fun contains(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        if (reader.hasNextToken("any")) {
            return acceptHandleNow(source) { item ->
                (item.itemMeta as? PotionMeta)?.hasCustomEffects() ?: false
            }
        } else {
            val raw = reader.readString()
            return acceptHandleFuture(source) { item ->
                raw.getOrNull(this).thenApply { name ->
                    val meta = item.itemMeta as? PotionMeta ?: return@thenApply item
                    val type = name?.asPotionEffectType() ?: return@thenApply item

                    return@thenApply meta.hasCustomEffect(type)
                }
            }
        }
    }

    private fun base(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        reader.mark()
        return when (val it = reader.nextToken()) {
            "type", "extended", "upgraded" -> {
                acceptHandleNow(source) { item ->
                    val meta = item.itemMeta as? PotionMeta ?: return@acceptHandleNow null
                    when (it) {
                        "type" -> meta.basePotionData.type
                        "extended" -> meta.basePotionData.isExtended
                        "upgraded" -> meta.basePotionData.isUpgraded
                        else -> meta.basePotionData
                    }
                }
            }
            "to" -> {
                val type = reader.readString()
                val extend = if (reader.hasNextToken("with", "by")) {
                    reader.readBoolean() to reader.readBoolean()
                } else {
                    BooleanLiveData(false) to BooleanLiveData(false)
                }

                acceptTransferFuture(source) { item ->
                    listOf(
                        type.getOrNull(this),
                        extend.first.getOrNull(this),
                        extend.second.getOrNull(this)
                    ).thenTake().thenApply { args ->
                        val meta = item.itemMeta as? PotionMeta ?: return@thenApply item
                        val potionData = PotionData(
                            args[0]?.toString()?.asPotionType() ?: PotionType.SLOWNESS,
                            args[1].coerceBoolean(false),
                            args[2].coerceBoolean(false)
                        )

                        meta.basePotionData = potionData
                        return@thenApply item.also { it.itemMeta = meta }
                    }
                }
            }
            else -> {
                acceptHandleNow(source) { item ->
                    (item.itemMeta as? PotionMeta)?.basePotionData
                }
            }
        }
    }

    private fun color(reader: QuestReader, source: LiveData<ItemStack>?): ActionItemStack.Handler {
        val color = reader.readColor()

        return acceptTransferFuture(source) { item ->
            color.get(this, Color.WHITE).thenApply { arg ->
                val meta = item.itemMeta as? PotionMeta ?: return@thenApply item
                meta.color = arg.toBukkit()
                return@thenApply item.also { it.itemMeta = meta }
            }
        }
    }

    private fun String.asPotionEffectType(): PotionEffectType? {
        return PotionEffectType.values().firstOrNull { it.name.equals(this, true) }
    }

    private fun String.asPotionType(): PotionType? {
        return PotionType.values().firstOrNull { it.name.equals(this, true) }
    }

    private fun Color.toBukkit(): org.bukkit.Color {
        return org.bukkit.Color.fromRGB(this.rgb)
    }
}