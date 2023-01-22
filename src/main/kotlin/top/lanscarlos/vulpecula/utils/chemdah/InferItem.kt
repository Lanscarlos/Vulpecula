package top.lanscarlos.vulpecula.utils

import ink.ptms.chemdah.api.event.InferItemHookEvent
import ink.ptms.chemdah.core.quest.selector.Flags.Companion.matchType
import ink.ptms.chemdah.util.startsWithAny
import ink.ptms.chemdah.util.substringAfterAny
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import taboolib.common.platform.function.warning
import taboolib.library.reflex.Reflex.Companion.invokeConstructor
import taboolib.module.kether.action.transform.CheckType
import taboolib.module.nms.getItemTag
import taboolib.module.nms.getName
import taboolib.platform.util.hasItem
import taboolib.platform.util.takeItem

/**
 * Chemdah
 * ink.ptms.chemdah.util.selector.InferItem
 *
 * [minecraft:]stone —— Vanilla Item
 * [minecraft:]stone[name=123,lore=123,enchant=123]
 *
 * zaphkiel:123[weight=1] —— Zaphkiel Item
 *
 * @author sky
 * @since 2021/4/5 9:32 下午
 */
class InferItem(val items: List<Item>) {

    fun isItem(item: ItemStack) = items.any { it.match(item) }

    fun check(inventory: Inventory, amount: Int): Boolean {
        return inventory.hasItem(amount) { items.any { item -> item.match(it) } }
    }

    fun take(inventory: Inventory, amount: Int): Boolean {
        return inventory.takeItem(amount) { items.any { item -> item.match(it) } }
    }

    open class Item(val material: String, val flags: List<Flags>, val data: List<DataMatch>) {

        open fun match(item: ItemStack) = matchType(item.type.name.lowercase()) && matchMetaData(item)

        open fun matchType(type: String) = flags.any { it.match(type, material) }

        @Suppress("SpellCheckingInspection")
        open fun matchMetaData(item: ItemStack): Boolean {
            val meta = item.itemMeta
            return data.all {
                when (it.key) {
                    // 名称
                    "name" -> it.check(item.getName(), CheckType.IN)
                    // 描述
                    "lore" -> it.check(meta?.lore.toString(), CheckType.IN)
                    // 附加值
                    "damage", "durability" -> it.check(item.durability.toInt())
                    // CMD
                    "custom-model-data" -> it.check(meta?.customModelData ?: 0)
                    // 附魔
                    "ench", "enchant", "enchants", "enchantment" -> meta?.enchants?.any { e -> it.check(e.key.name) } ?: false
                    // 药水
                    "potion", "potions" -> if (meta is PotionMeta) {
                        it.check(meta.basePotionData.type.name) || meta.customEffects.any { e -> it.check(e.type.name) }
                    } else {
                        false
                    }
                    // 其他选项
                    else -> when {
                        // 附魔等级
                        it.key.startsWithAny("ench.", "enchant.", "enchantment.") -> {
                            val ench = it.key.substringAfterAny("ench.", "enchant.", "enchantment.")
                            val level = meta?.enchants?.entries?.firstOrNull { e -> e.key.name.equals(ench, true) } ?: 0
                            it.check(level)
                        }
                        // NBT
                        it.key.startsWithAny("nbt.", "tag.") -> {
                            val data = item.getItemTag().getDeep(it.key.substringAfterAny("nbt.", "tag."))?.unsafeData()
                            if (data != null) it.check(data) else false
                        }
                        // 扩展
                        else -> matchMetaData(item, meta, it)
                    }
                }
            }
        }

        open fun matchMetaData(item: ItemStack, itemMeta: ItemMeta?, dataMatch: DataMatch): Boolean {
            warning("$material[${dataMatch.key} ${dataMatch.type} ${dataMatch.value}] not supported.")
            return false
        }

        open fun check(inventory: Inventory, amount: Int): Boolean {
            return inventory.hasItem(amount) { match(it) }
        }

        open fun take(inventory: Inventory, amount: Int): Boolean {
            return inventory.takeItem(amount) { match(it) }
        }
    }

    companion object {

        fun List<String>.toInferItem() = InferItem(map { it.toInferItem() })

        @Suppress("DuplicatedCode")
        fun String.toInferItem(): Item {
            var type: String
            val data = arrayListOf<DataMatch>()
            val flag = ArrayList<Flags>()
            if (indexOf('[') > -1 && endsWith(']')) {
                type = substring(0, indexOf('['))
                data += substring(indexOf('[') + 1, length - 1).split(',').map { DataMatch.fromString(it.trim()) }
            } else {
                type = this
            }
            val indexOfType = type.indexOf(':')
            val item = if (indexOfType in 0..(type.length - 2)) {
                val item = when (val namespace = type.substring(0, indexOfType)) {
                    "minecraft" -> Item::class.java
                    else -> InferItemHookEvent(namespace, Item::class.java).apply { call() }.itemClass
                }
                type = type.substring(indexOfType + 1)
                item
            } else {
                Item::class.java
            }
            return item.invokeConstructor(type.matchType(flag), flag, data)
        }
    }
}