package top.lanscarlos.vulpecula.kether

import com.mojang.datafixers.kinds.App
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.common5.*
import taboolib.library.kether.Parser
import taboolib.library.kether.QuestReader
import taboolib.library.xseries.XMaterial
import taboolib.module.kether.ParserHolder
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import taboolib.module.kether.run
import taboolib.platform.type.BukkitPlayer
import taboolib.platform.util.buildItem
import taboolib.platform.util.toProxyLocation
import top.lanscarlos.vulpecula.kether.live.LiveData
import top.lanscarlos.vulpecula.kether.live.readBoolean
import top.lanscarlos.vulpecula.kether.live.readInt
import top.lanscarlos.vulpecula.utils.*
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2023-02-12 11:10
 */
interface ParserBuilder {

    fun <T> now(action: ScriptFrame.() -> T): Parser.Action<T> = ParserHolder.now(action)

    fun <T> future(action: ScriptFrame.() -> CompletableFuture<T>): Parser.Action<T> = ParserHolder.future(action)

    fun boolean(def: Boolean? = null): Parser<Boolean> = booleanOrNull().map { it ?: def ?: error("No boolean selected.") }

    fun booleanOrNull(): Parser<Boolean?> {
        return Parser.frame { reader ->
            reader.mark()

            when (reader.nextToken()) {
                "true", "yes" -> now { true }
                "false", "no" -> now { false }
                else -> {
                    reader.reset()
                    val action = reader.nextBlock()
                    future {
                        this.run(action).thenApply {
                            it?.cbool
                        }
                    }
                }
            }
        }
    }

    fun short(def: Short? = null): Parser<Short> = shortOrNull().map { it ?: def ?: error("No short selected.") }

    fun shortOrNull(): Parser<Short?> {
        return Parser.frame { reader ->
            reader.mark()

            reader.nextToken().toShortOrNull()?.let {
                now { it }
            } ?: reader.reset().let {
                val action = reader.nextBlock()
                future {
                    this.run(action).thenApply {
                        it?.cshort
                    }
                }
            }
        }
    }

    fun int(def: Int? = null): Parser<Int> = intOrNull().map { it ?: def ?: error("No int selected.") }

    fun intOrNull(): Parser<Int?> {
        return Parser.frame { reader ->
            reader.mark()

            reader.nextToken().toIntOrNull()?.let {
                now { it }
            } ?: reader.reset().let {
                val action = reader.nextBlock()
                future {
                    this.run(action).thenApply {
                        it?.cint
                    }
                }
            }
        }
    }

    fun long(def: Long? = null): Parser<Long> = longOrNull().map { it ?: def ?: error("No long selected.") }

    fun longOrNull(): Parser<Long?> {
        return Parser.frame { reader ->
            reader.mark()

            reader.nextToken().toLongOrNull()?.let {
                now { it }
            } ?: reader.reset().let {
                val action = reader.nextBlock()
                future {
                    this.run(action).thenApply {
                        it?.clong
                    }
                }
            }
        }
    }

    fun float(def: Float? = null): Parser<Float> = floatOrNull().map { it ?: def ?: error("No float selected.") }

    fun floatOrNull(): Parser<Float?> {
        return Parser.frame { reader ->
            reader.mark()

            reader.nextToken().toFloatOrNull()?.let {
                now { it }
            } ?: reader.reset().let {
                val action = reader.nextBlock()
                future {
                    this.run(action).thenApply {
                        it?.cfloat
                    }
                }
            }
        }
    }

    fun double(def: Double? = null): Parser<Double> = doubleOrNull().map { it ?: def ?: error("No double selected.") }

    fun doubleOrNull(): Parser<Double?> {
        return Parser.frame { reader ->
            reader.mark()

            reader.nextToken().toDoubleOrNull()?.let {
                now { it }
            } ?: reader.reset().let {
                val action = reader.nextBlock()
                future {
                    this.run(action).thenApply {
                        it?.cdouble
                    }
                }
            }
        }
    }

    fun string(def: String? = null): Parser<String> = stringOrNull().map { it ?: def ?: error("No text selected.") }

    fun stringOrNull(): Parser<String?> = frame {
        it?.toString()
    }

    fun location(def: Location? = null): Parser<Location> = frame {
        when (it) {
            is Location -> it
            is org.bukkit.Location -> it.toProxyLocation()
            is ProxyPlayer -> it.location
            is Entity -> it.location.toProxyLocation()
            is Vector -> Location(def?.world, it.x, it.y, it.z)
            is org.bukkit.util.Vector -> Location(def?.world, it.x, it.y, it.z)
            else -> def ?: error("No location selected.")
        }
    }

    fun locationOrNull(): Parser<Location?> = frame {
        when (it) {
            is Location -> it
            is org.bukkit.Location -> it.toProxyLocation()
            is ProxyPlayer -> it.location
            is Entity -> it.location.toProxyLocation()
            is Vector -> Location(null, it.x, it.y, it.z)
            is org.bukkit.util.Vector -> Location(null, it.x, it.y, it.z)
            is String -> {
                /*
                * world,x,y,z
                * world,x,y,z,yaw.pitch
                * */
                if (!it.matches("^[A-Za-z0-9_\\- \\u4e00-\\u9fa5]+,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?(-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?)?\$".toRegex())) return@frame null
                val demand = it.split(",")
                Location(
                    demand[0], demand[1].toDouble(), demand[2].toDouble(), demand[3].toDouble(),
                    demand.getOrNull(4)?.toFloatOrNull() ?: 0f,
                    demand.getOrNull(5)?.toFloatOrNull() ?: 0f
                )
            }
            else -> null
        }
    }

    fun vector(def: Vector?): Parser<Vector> = vectorOrNull().map { it ?: def ?: error("No vector selected.") }

    fun vectorOrNull(): Parser<Vector?> = frame {
        when (it) {
            is Vector -> it
            is org.bukkit.util.Vector -> Vector(it.x, it.y, it.z)
            is Location -> it.direction
            is org.bukkit.Location -> it.toProxyLocation().direction
            is String -> {
                // x,y,z
                if (!it.matches("^\\d+(\\.\\d+)?,\\d+(\\.\\d+)?,\\d+(\\.\\d+)?\$".toRegex())) return@frame null
                val demand = it.split(",").map { el -> el.toDouble() }
                Vector(demand[0], demand[1], demand[2])
            }
            else -> null
        }
    }

    fun colorOrNull(): Parser<Color?> = frame {
        when (it) {
            is Color -> it
            is org.bukkit.Color -> Color(it.red, it.green, it.blue)
            is String -> {
                if (it.startsWith('#') && it.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})\$".toRegex())) {
                    // hex
                    val hex = it.substring(1)
                    Color.decode(hex)
                } else if (it.matches("^((2[0-4][0-9]|25[0-5])(,|-)){2}(2[0-4][0-9]|25[0-5])\$".toRegex())) {
                    val demand = it.split(",").map { el -> el.toInt() }
                    Color(demand[0], demand[1], demand[2])
                } else {
                    val rgb = it.toIntOrNull() ?: return@frame null
                    Color(rgb)
                }
            }
            else -> null
        }
    }

    fun entity(def: Entity? = null): Parser<Entity> = entityOrNull().map { it ?: def ?: error("No entity selected.") }

    fun entityOrNull(): Parser<Entity?> = frame {
        when (it) {
            is Entity -> it
            is ProxyPlayer -> (it as? BukkitPlayer)?.player
            is String -> Bukkit.getPlayerExact(it)
            else -> null
        }
    }

    fun item(def: ItemStack?): Parser<ItemStack> = itemOrNull().map { it ?: def ?: error("No item selected.") }

    fun itemOrNull(): Parser<ItemStack?> = frame {
        when (it) {
            is ItemStack -> it
            is Item -> it.itemStack
            is String -> {
                val material = XMaterial.matchXMaterial(it.uppercase()).let { mat ->
                    if (mat.isPresent) mat.get() else return@frame null
                }
                buildItem(material)
            }
            else -> null
        }
    }

    fun <T> option(vararg expected: String, then: Parser<T>): Parser<T?> {
        return Parser.frame { reader ->
            if (reader.hasNextToken(*expected)) {
                then.reader.apply(reader)
            } else {
                Parser.Action.point(null)
            }
        }
    }

    fun <T> trim(vararg expected: String, then: Parser<T>): Parser<T> {
        return Parser.frame { reader ->
            reader.hasNextToken(*expected)
            then.reader.apply(reader)
        }
    }

    fun arguments(
        vararg mapper: Pair<Array<String>, Parser<*>>,
        prefix: String = "-"
    ): Parser<Map<String, Any>> {
        return Parser.frame { reader ->
            val cache = mutableMapOf<String, Parser.Action<*>>()
            while (reader.nextPeek().startsWith(prefix)) {
                val key = reader.nextToken().substring(prefix.length)
                val parser = mapper.firstOrNull { key in it.first } ?: continue
                cache[parser.first.first()] = parser.second.reader.apply(reader)
            }
            future {
                val futures = cache.mapValues { it.value.run(this) }
                CompletableFuture.allOf(*futures.values.toTypedArray()).thenApply {
                    futures.mapValues { it.value.getNow(null) }
                }
            }
        }
    }

    fun <T> frame(func: (Any?) -> T): Parser<T> {
        return Parser.frame { reader ->
            val action = reader.nextBlock()
            Parser.Action { frame ->
                frame.run(action).thenApply {
                    func(it)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <P1, R> group(
        p1: Parser<P1>,
        func: (P1) -> Parser.Action<R>
    ): Parser<Parser.Action<R>> {
        val instance = Parser.instance()
        return instance.group(p1).apply(instance, func) as Parser<Parser.Action<R>>
    }

    @Suppress("UNCHECKED_CAST")
    fun <P1, P2, R> group(
        p1: Parser<P1>,
        p2: Parser<P2>,
        func: (P1, P2) -> Parser.Action<R>
    ): Parser<Parser.Action<R>>  {
        val instance = Parser.instance()
        return instance.group(p1, p2).apply(instance, func) as Parser<Parser.Action<R>>
    }

    @Suppress("UNCHECKED_CAST")
    fun <P1, P2, P3, R> group(
        p1: Parser<P1>,
        p2: Parser<P2>,
        p3: Parser<P3>,
        func: (P1, P2, P3) -> Parser.Action<R>
    ): Parser<Parser.Action<R>> {
        val instance = Parser.instance()
        info("group...")
        return instance.group(p1, p2, p3).apply(instance, func).also { info("done...") } as Parser<Parser.Action<R>>
    }

    @Suppress("UNCHECKED_CAST")
    fun <P1, P2, P3, P4, R> group(
        p1: Parser<P1>,
        p2: Parser<P2>,
        p3: Parser<P3>,
        p4: Parser<P4>,
        func: (P1, P2, P3, P4) -> Parser.Action<R>
    ): Parser<Parser.Action<R>> {
        val instance = Parser.instance()
        return instance.group(p1, p2, p3, p4).apply(instance, func) as Parser<Parser.Action<R>>
    }

    @Suppress("UNCHECKED_CAST")
    fun <P1, P2, P3, P4, P5, R> group(
        p1: Parser<P1>,
        p2: Parser<P2>,
        p3: Parser<P3>,
        p4: Parser<P4>,
        p5: Parser<P5>,
        func: (P1, P2, P3, P4, P5) -> Parser.Action<R>
    ): Parser<Parser.Action<R>> {
        val instance = Parser.instance()
        return instance.group(p1, p2, p3, p4, p5).apply(instance, func) as Parser<Parser.Action<R>>
    }
}