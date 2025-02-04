package top.lanscarlos.vulpecula.module.item

import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.item
 *
 * 物品流
 *
 * @author Lanscarlos
 * @since 2025-01-13 12:56
 */
interface ItemStream {

    /**
     * 应用物品修改
     */
    fun apply(): ItemStack

    /**
     * 销毁物品
     */
    fun destroy()

    /**
     * 获取物品材质
     */
    fun getMaterial(): XMaterial

    /**
     * 设置物品材质
     */
    fun setMaterial(material: XMaterial)

    /**
     * 获取物品数量
     */
    fun getAmount(): Int

    /**
     * 设置物品数量
     */
    fun setAmount(amount: Int)

    /**
     * 获取物品损耗值
     */
    fun getDamage(): Int

    /**
     * 设置物品损耗值
     */
    fun setDamage(damage: Int)

    /**
     * 获取物品最大耐久度
     */
    fun getDurability(): Int

    /**
     * 设置物品耐久度
     */
    fun setDurability(durability: Int)

    /**
     * 获取物品最大耐久度
     */
    fun getMaxDurability(): Int

    /**
     * 设置物品展示名
     */
    fun getDisplayName(): String?

    /**
     * 获取物品展示名
     */
    fun setDisplayName(name: String?)

    /**
     * 获取物品描述
     */
    fun getLore(): List<String>

    /**
     * 设置物品描述
     */
    fun setLore(lore: List<String>)

    /**
     * 清除物品描述
     */
    fun clearLore()

    /**
     * 追加物品描述
     */
    fun appendLore(lore: List<String>)

    /**
     * 获取物品标识
     */
    fun getFlags(): Set<ItemFlag>

    /**
     * 设置物品标识
     */
    fun setFlags(flags: Set<ItemFlag>)

    /**
     * 移除指定物品标识
     */
    fun removeFlags(vararg flag: ItemFlag)

    /**
     * 获取附魔
     */
    fun getEnchantments(): Map<Enchantment, Int>

    /**
     * 设置附魔
     */
    fun setEnchantments(enchantments: Map<Enchantment, Int>)

    /**
     * 清除所有附魔
     */
    fun clearEnchantments()

    /**
     * 追加附魔
     */
    fun appendEnchantments(enchantments: Map<Enchantment, Int>)

    /**
     * 判断是否拥有指定附魔
     */
    fun hasEnchantment(enchantment: Enchantment): Boolean

    /**
     * 判断是否拥有与之冲突的附魔
     */
    fun hasConflictingEnchant(enchantment: Enchantment): Boolean

    /**
     * 获取指定附魔等级
     */
    fun getEnchantmentLevel(enchantment: Enchantment): Int

    /**
     * 设置指定附魔等级
     *
     * @param enchantment 附魔
     * @param level 等级
     */
    fun setEnchantmentLevel(enchantment: Enchantment, level: Int)

    /**
     * 设置指定附魔等级
     *
     * @param enchantment 附魔
     * @param level 等级
     * @param ignoreLevelRestriction 是否忽略等级限制
     */
    fun setEnchantmentLevel(enchantment: Enchantment, level: Int, ignoreLevelRestriction: Boolean)

    /**
     * 移除指定附魔
     */
    fun removeEnchantment(enchantment: Enchantment)

    /**
     * 获取物品颜色
     */
    fun getColor(): Color?

    /**
     * 设置物品颜色
     */
    fun setColor(color: Color?)

    /**
     * 混合颜色
     */
    fun mixColor(vararg color: Color)

    /**
     * 混合染料颜色
     */
    fun mixDyeColor(vararg color: DyeColor)

}