package top.lanscarlos.vulpecula.utils

import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.*
import java.awt.Color

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
fun <T> LiveData<*>?.getValue(frame: ScriptFrame, def: T): T {
    if (this == null) return def
    return (this as? LiveData<T>)?.get(frame, def) ?: def
}

/**
 * 以兼容模式读取 Boolean
 * */
fun QuestReader.readBoolean(): LiveData<Boolean> = BooleanLiveData.read(reader = this)

/**
 * 以兼容模式读取 Int
 * */
fun QuestReader.readInt(): LiveData<Int> = IntLiveData.read(reader = this)

/**
 * 以兼容模式读取 Double
 * */
fun QuestReader.readDouble(): LiveData<Double> = DoubleLiveData.read(reader = this)

/**
 * 以兼容模式读取 String
 * */
fun QuestReader.readString(): LiveData<String> = StringLiveData.read(reader = this)

/**
 * 以兼容模式读取 List<String>
 * */
fun QuestReader.readStringList(): LiveData<List<String>> = StringListLiveData.read(reader = this)

/**
 * 以兼容模式读取 Color
 * */
fun QuestReader.readColor(): LiveData<Color> = ColorLiveData.read(reader = this)

/**
 * 以兼容模式读取 Vector
 * */
fun QuestReader.readVector(): LiveData<Vector> = VectorLiveData.read(reader = this)

/**
 * 以兼容模式读取 Location
 * */
fun QuestReader.readLocation(): LiveData<Location> = LocationLiveData.read(reader = this)

/**
 * 以兼容模式读取 ItemStack
 * */
fun QuestReader.readItemStack(): LiveData<ItemStack> = ItemLiveData.read(reader = this)