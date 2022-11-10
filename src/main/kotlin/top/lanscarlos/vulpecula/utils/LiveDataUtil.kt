package top.lanscarlos.vulpecula.utils

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
 * 以兼容模式读取 Vector
 * */
fun QuestReader.readVector(): LiveData<Vector> = VectorLiveData.read(reader = this)

/**
 * 以兼容模式读取 Location
 * */
fun QuestReader.readLocation(): LiveData<Location> = LocationLiveData.read(reader = this)

/**
 * 以兼容模式读取 Color
 * */
fun QuestReader.readColor(): LiveData<Color> = ColorLiveData.read(reader = this)

/**
 * 以兼容模式读取 List<String>
 * */
fun QuestReader.readStringList(): LiveData<List<String>> = StringListLiveData.read(reader = this)

//fun ScriptFrame.coerceParticle(input: Any?, def: ProxyParticle): ProxyParticle {
//    return when (input) {
//        is ProxyParticle -> input
//        is ParsedAction<*> -> {
//            this.run(input).join()?.toString()?.let { p ->
//                ProxyParticle.valueOf(p.uppercase())
//            } ?: def
//        }
//        is String -> ProxyParticle.valueOf(input)
//        else -> def
//    }
//}

//fun ScriptFrame.coerceInt(input: Any?, def: Int): Int {
//    return when (input) {
//        is Int -> input
//        is ParsedAction<*> -> this.run(input).join().toInt(def)
//        else -> input.toInt(def)
//    }
//}

//fun ScriptFrame.coerceDouble(input: Any?, def: Double): Double {
//    return when (input) {
//        is Int -> input.toDouble()
//        is Double -> input
//        is ParsedAction<*> -> this.run(input).join().toDouble(def)
//        else -> input.toDouble(def)
//    }
//}

//fun ScriptFrame.coerceString(input: Any?, def: String): String {
//    return when (input) {
//        is String -> input
//        is Number -> input.toString()
//        is ParsedAction<*> -> this.run(input).join()?.toString() ?: def
//        else -> input?.toString() ?: def
//    }
//}

//fun ScriptFrame.coerceVector(input: Any?, def: Vector): Vector {
//    return when (input) {
//        is Vector -> input
//        is org.bukkit.util.Vector -> Vector(input.x, input.y, input.z)
//        is ParsedAction<*> -> {
//            when (val result = this.run(input).join()) {
//                is Vector -> result
//                is org.bukkit.util.Vector -> Vector(result.x, result.y, result.z)
//                is Location -> result.toVectorFix()
//                is org.bukkit.Location -> result.toProxyLocation().toVectorFix()
//                else -> def
//            }
//        }
//        is Triple<*, *, *> -> {
//            Vector(
//                (input.first as? ParsedAction<*>)?.let { action -> this.run(action).join().toDouble(def.x) } ?: def.x,
//                (input.second as? ParsedAction<*>)?.let { action -> this.run(action).join().toDouble(def.y) } ?: def.y,
//                (input.third as? ParsedAction<*>)?.let { action -> this.run(action).join().toDouble(def.z) } ?: def.z
//            )
//        }
//        else -> def
//    }
//}

//fun ScriptFrame.coerceLocation(input: Any?, def: Location): Location {
//    return when (input) {
//        is Location -> input
//        is org.bukkit.Location -> input.toProxyLocation()
//        is Entity -> input.location.toProxyLocation()
//        is ParsedAction<*> -> {
//            when (val result = this.run(input).join()) {
//                is Location -> result
//                is org.bukkit.Location -> result.toProxyLocation()
//                is Entity -> result.location.toProxyLocation()
//                else -> def
//            }
//        }
//        is Triple<*, *, *> -> {
//            when (input.first) {
//                is Double -> Location(def.world, input.first.toDouble(0.0), input.second.toDouble(0.0), input.third.toDouble(0.0))
//                is ParsedAction<*> -> {
//                    Location(
//                        def.world,
//                        (input.first as? ParsedAction<*>)?.let { this.run(it).join() }.toDouble(0.0),
//                        (input.second as? ParsedAction<*>)?.let { this.run(it).join() }.toDouble(0.0),
//                        (input.third as? ParsedAction<*>)?.let { this.run(it).join() }.toDouble(0.0),
//                    )
//                }
//                else -> def
//            }
//        }
//        else -> def
//    }
//}

//fun ScriptFrame.coerceColor(input: Any?, def: Color): Color {
//    return when (input) {
//        is Color -> input
//        is ParsedAction<*> -> {
//            this.run(input).join()?.toString()?.let { hex ->
//                Color.decode(if (hex.startsWith('#')) hex.substring(1) else hex)
//            } ?: def
//        }
//        else -> def
//    }
//}