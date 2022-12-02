package top.lanscarlos.vulpecula.kether.live

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.utils
 *
 * @author Lanscarlos
 * @since 2022-11-10 14:56
 */

/**
 * 根据传入的默认值的类型自动匹配 LiveData
 * */
fun <T : Any> LiveData<*>?.getValue(frame: ScriptFrame, def: T): CompletableFuture<T> {
    if (this == null) return CompletableFuture.completedFuture(def)
    val it: CompletableFuture<*>? = when (def) {
        is Boolean -> (this as BooleanLiveData).get(frame, def)
        is Int -> (this as IntLiveData).get(frame, def)
        is Double -> (this as DoubleLiveData).get(frame, def)
        is String -> (this as StringLiveData).get(frame, def)
        is List<*> -> (this as StringListLiveData).get(frame, def.map { it.toString() })
        is Color -> (this as ColorLiveData).get(frame, def)
        is Vector -> (this as VectorLiveData).get(frame, def)
        is Location -> (this as LocationLiveData).get(frame, def)
        is ItemStack -> (this as ItemLiveData).get(frame, def)
        is Entity -> (this as EntityLiveData).get(frame, def)
        else -> (this as? LiveData<T>)?.get(frame, def)
    }
    return it as? CompletableFuture<T> ?: CompletableFuture.completedFuture(def)
}

/**
 * 根据传入的默认值的类型自动匹配 LiveData
 * */
fun <T: Any> LiveData<*>?.getValueOrNull(frame: ScriptFrame): CompletableFuture<T?> {
    if (this == null) return CompletableFuture.completedFuture(null)
    return this.getOrNull(frame) as? CompletableFuture<T?> ?: CompletableFuture.completedFuture(null)
}

/**
 * 以兼容模式读取 Boolean
 * */
fun QuestReader.readBoolean(): LiveData<Boolean> = BooleanLiveData.read(reader = this)

fun QuestReader.tryReadBoolean(vararg expect: String): LiveData<Boolean>? {
    return if (this.hasNextToken(*expect)) {
        BooleanLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 Int
 * */
fun QuestReader.readInt(): LiveData<Int> = IntLiveData.read(reader = this)

fun QuestReader.tryReadInt(vararg expect: String): LiveData<Int>? {
    return if (this.hasNextToken(*expect)) {
        IntLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 Double
 * */
fun QuestReader.readDouble(): LiveData<Double> = DoubleLiveData.read(reader = this)

fun QuestReader.tryReadDouble(vararg expect: String): LiveData<Double>? {
    return if (this.hasNextToken(*expect)) {
        DoubleLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 String
 * */
fun QuestReader.readString(): LiveData<String> = StringLiveData.read(reader = this)

fun QuestReader.tryReadString(vararg expect: String): LiveData<String>? {
    return if (this.hasNextToken(*expect)) {
        StringLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 List<String>
 * */
fun QuestReader.readStringList(): LiveData<List<String>> = StringListLiveData.read(reader = this)

fun QuestReader.tryReadStringList(vararg expect: String): LiveData<List<String>>? {
    return if (this.hasNextToken(*expect)) {
        StringListLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 Color
 * */
fun QuestReader.readColor(): LiveData<Color> = ColorLiveData.read(reader = this)

fun QuestReader.tryReadColor(vararg expect: String): LiveData<Color>? {
    return if (this.hasNextToken(*expect)) {
        ColorLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 Vector
 * */
fun QuestReader.readVector(produce: Boolean): LiveData<Vector> = VectorLiveData.read(reader = this, produce)

fun QuestReader.tryReadVector(vararg expect: String, produce: Boolean): LiveData<Vector>? {
    return if (this.hasNextToken(*expect)) {
        VectorLiveData.read(reader = this, produce)
    } else null
}

/**
 * 以兼容模式读取 Location
 * */
fun QuestReader.readLocation(): LiveData<Location> = LocationLiveData.read(reader = this)

fun QuestReader.tryReadLocation(vararg expect: String): LiveData<Location>? {
    return if (this.hasNextToken(*expect)) {
        LocationLiveData.read(reader = this)
    } else null
}

/**
 * 以兼容模式读取 ItemStack
 * */
fun QuestReader.readItemStack(): LiveData<ItemStack> = ItemLiveData(this.nextBlock())

fun QuestReader.tryReadItemStack(vararg expect: String): LiveData<ItemStack>? {
    return if (this.hasNextToken(*expect)) {
        ItemLiveData(this.nextBlock())
    } else null
}

/**
 * 以兼容模式读取 Entity
 * */
fun QuestReader.readEntity(): LiveData<Entity> = EntityLiveData(this.nextBlock())

fun QuestReader.tryReadEntity(vararg expect: String): LiveData<Entity>? {
    return if (this.hasNextToken(*expect)) {
        EntityLiveData(this.nextBlock())
    } else null
}

/**
 * 以兼容模式读取 Collection
 * */
fun QuestReader.readCollection(): LiveData<Collection<*>> = CollectionLiveData(this.nextBlock())

fun QuestReader.tryReadCollection(vararg expect: String): LiveData<Collection<*>>? {
    return if (this.hasNextToken(*expect)) {
        CollectionLiveData(this.nextBlock())
    } else null
}