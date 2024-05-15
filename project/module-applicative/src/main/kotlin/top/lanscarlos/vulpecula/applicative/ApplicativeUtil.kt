package top.lanscarlos.vulpecula.applicative

import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import java.awt.Color

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 17:46
 */

/**
 * 将对象转换为 Boolean
 *
 * @param def 默认值
 * */
fun Any?.applicativeBoolean(def: Boolean = false): Boolean {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Boolean::class.java) ?: BooleanApplicative
    return applicative.apply(this, def)
}

/**
 * 将对象转换为 Int
 *
 * @param def 默认值
 * */
fun Any?.applicativeInt(def: Int = 0): Int {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Int::class.java) ?: IntApplicative
    return applicative.apply(this, def)
}

/**
 * 将对象转换为 Long
 *
 * @param def 默认值
 * */
fun Any?.applicativeLong(def: Long = 0L): Long {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Long::class.java) ?: LongApplicative
    return applicative.apply(this, def)
}

/**
 * 将对象转换为 Float
 *
 * @param def 默认值
 * */
fun Any?.applicativeFloat(def: Float = 0.0f): Float {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Float::class.java) ?: FloatApplicative
    return applicative.apply(this, def)
}

/**
 * 将对象转换为 Double
 *
 * @param def 默认值
 * */
fun Any?.applicativeDouble(def: Double = 0.0): Double {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Double::class.java) ?: DoubleApplicative
    return applicative.apply(this) ?: def
}

/**
 * 将对象转换为 java.awt.Color
 *
 * @param def 默认值
 * */
fun Any?.applicativeColor(def: Color = Color.WHITE): Color {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Color::class.java) ?: ColorApplicative
    return applicative.apply(this, def)
}

/**
 * 将对象转换为 taboolib.common.util.Vector
 *
 * @param def 默认值
 * */
fun Any?.applicativeVector(def: Vector = Vector(0.0, 0.0, 0.0)): Vector {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Vector::class.java) ?: VectorApplicative
    return applicative.apply(this, def)
}

/**
 * 将对象转换为 taboolib.common.util.Location
 *
 * @param def 默认值
 * */
fun Any?.applicativeLocation(def: Location = Location(null, 0.0, 0.0, 0.0)): Location {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Location::class.java) ?: LocationApplicative
    return applicative.apply(this, def)
}

fun Any?.applicativeItemStack(def: ItemStack = ItemStack(Material.STONE)): ItemStack {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(ItemStack::class.java) ?: ItemStackApplicative
    return applicative.apply(this, def)
}

fun Any?.applicativeEntity(def: Entity? = null): Entity? {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Entity::class.java) ?: EntityApplicative
    return applicative.apply(this, def ?: return null)
}

fun Any?.applicativePlayer(def: Player? = null): Player? {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Player::class.java) ?: PlayerApplicative
    return applicative.apply(this, def ?: return null)
}

fun Any?.applicativeInventory(def: Inventory? = null): Inventory? {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(Inventory::class.java) ?: InventoryApplicative
    return applicative.apply(this, def ?: return null)
}

fun Any?.applicativeIntList(def: List<Int> = emptyList()): List<Int> {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(List::class.java)
    return applicative?.apply(this, def)?.mapIndexed { index, it ->
        it.applicativeInt(def.getOrNull(index) ?: 0)
    } ?: def
}

fun Any?.applicativeStringList(def: List<String> = emptyList()): List<String> {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(List::class.java)
    return applicative?.apply(this, def)?.mapIndexed { index, it ->
        it?.toString() ?: def.getOrNull(index) ?: "null"
    } ?: def
}

fun Any?.applicativePlayerList(def: List<Player> = emptyList()): List<Player> {
    if (this == null) {
        return def
    }
    val applicative = ApplicativeRegistry.getApplicative(List::class.java)
    return applicative?.apply(this, def)?.mapIndexedNotNull { index, it ->
        it.applicativePlayer(def.getOrNull(index))
    } ?: def
}