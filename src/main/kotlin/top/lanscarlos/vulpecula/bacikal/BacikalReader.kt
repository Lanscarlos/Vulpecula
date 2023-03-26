package top.lanscarlos.vulpecula.bacikal

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.common5.*
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.frameBy
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveColor
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveEntity
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveInventory
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveItemStack
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveLocation
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.livePlayer
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveStringList
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveVector
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.readerOf
import top.lanscarlos.vulpecula.bacikal.action.ActionBlock
import top.lanscarlos.vulpecula.utils.nextBlock
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-02-26 16:39
 */
open class BacikalReader(private val source: QuestReader) {

    val argumentPrefixPattern = "-\\D+".toRegex()
    val methods = HashMap<String, () -> Bacikal.Parser<Any?>>()
    var other: (() -> Bacikal.Parser<Any?>)? = null
        private set

    fun case(vararg name: String, func: () -> Bacikal.Parser<Any?>) {
        name.forEach { methods[it] = func }
    }

    fun other(func: () -> Bacikal.Parser<Any?>) {
        other = func
    }

    /*
    * QuestReader 操作
    * */

    fun mark(): Int {
        source.mark()
        return source.mark
    }

    fun reset() {
        source.reset()
    }

    fun nextToken(): String {
        return source.nextToken()
    }

    fun peekToken(): String {
        source.mark()
        return source.nextToken().also { source.reset() }
    }

    fun expectToken(vararg expect: String): Boolean {
        if (expect.isEmpty()) return false
        source.mark()
        return if (source.nextToken() in expect) {
            true
        } else {
            source.reset()
            false
        }
    }

    fun readAction(): ParsedAction<*> {
        return if (this.expectToken("{")) {
            val actions = mutableListOf<ParsedAction<*>>()
            while (!expectToken("}")) {
                actions += source.nextParsedAction()
            }
            ParsedAction(ActionBlock(actions))
        } else {
            source.nextParsedAction()
        }
    }

    fun readActionList(): List<ParsedAction<*>> {
        return source.next(ArgTypes.listOf(ArgTypes.ACTION))
    }

    /*
    * Action<T> 函数
    * */

    fun <R> now(func: ScriptFrame.() -> R): Bacikal.Action<R> {
        return Bacikal.Action { CompletableFuture.completedFuture(func(it)) }
    }

    fun <R> future(func: ScriptFrame.() -> CompletableFuture<R>): Bacikal.Action<R> {
        return Bacikal.Action(func)
    }

    /*
    * LiveData 操作
    * */

    fun <T> trim(vararg expect: String, then: LiveData<T>): LiveData<T> {
        return then.trim(*expect)
    }

    fun <T> expect(vararg expect: String, then: LiveData<T>): LiveData<T> {
        return then.expect(*expect)
    }

    fun <T> optional(vararg expect: String, then: LiveData<T>): LiveData<T?> {
        return then.optional(*expect)
    }

    fun <T> optional(vararg expect: String, then: LiveData<T>, def: T): LiveData<T> {
        return then.optional(*expect, def = def)
    }

    /**
     * 额外参数 <br>
     * 适用于 -prefix {xxx} 的情况
     * @param prefix 参数识别前缀
     * @param then 参数处理
     * */
    fun <T> argument(vararg prefix: String, then: LiveData<T>): LiveData<T?> {
        return LiveDataProxy(*prefix, source = then.optional(), def = null)
    }

    /**
     * 额外参数 <br>
     * 适用于 -prefix {xxx} 的情况
     * @param prefix 参数识别前缀
     * @param then 参数处理
     * @param def 默认值
     * */
    fun <T> argument(vararg prefix: String, then: LiveData<T>, def: T): LiveData<T> {
        return LiveDataProxy(*prefix, source = then, def = def)
    }

    fun action(): LiveData<ParsedAction<*>> {
        return readerOf { it.readAction() }
    }

    fun any(): LiveData<Any?> = frameBy { it }

    fun list(): LiveData<List<*>> = LiveData {
        val list = this.readActionList()
        Bacikal.Action { frame ->
            list.map { frame.run(it) }.union()
        }
    }

    fun boolOrNull(): LiveData<Boolean?> {
        return LiveData {
            source.mark()
            source.nextToken()
            when (source.nextToken()) {
                "true", "yes" -> Bacikal.Action { CompletableFuture.completedFuture(true) }
                "false", "no" -> Bacikal.Action { CompletableFuture.completedFuture(true) }
                else -> {
                    source.reset()
                    val action = source.nextBlock()
                    Bacikal.Action { frame ->
                        frame.run(action).thenApply { it?.cbool }
                    }
                }
            }
        }
    }

    fun bool(def: Boolean? = null, display: String = "boolean"): LiveData<Boolean> {
        return boolOrNull().map { it ?: def ?: error("No $display selected.") }
    }

    fun intOrNull(): LiveData<Int?> {
        return LiveData {
            source.mark()
            source.nextToken().toIntOrNull()?.let { int ->
                Bacikal.Action {
                    CompletableFuture.completedFuture(int)
                }
            } ?: source.reset().let {
                val action = source.nextBlock()
                Bacikal.Action { frame ->
                    frame.run(action).thenApply { it?.cint }
                }
            }
        }
    }

    fun int(def: Int? = null, display: String = "int"): LiveData<Int> {
        return intOrNull().map { it ?: def ?: error("No $display selected.") }
    }

    fun longOrNull(): LiveData<Long?> {
        return LiveData {
            source.mark()
            source.nextToken().toLongOrNull()?.let { long ->
                Bacikal.Action {
                    CompletableFuture.completedFuture(long)
                }
            } ?: source.reset().let {
                val action = source.nextBlock()
                Bacikal.Action { frame ->
                    frame.run(action).thenApply { it?.clong }
                }
            }
        }
    }

    fun long(def: Long? = null, display: String = "long"): LiveData<Long> {
        return longOrNull().map { it ?: def ?: error("No $display selected.") }
    }

    fun floatOrNull(): LiveData<Float?> {
        return LiveData {
            source.mark()
            source.nextToken().toFloatOrNull()?.let { float ->
                Bacikal.Action {
                    CompletableFuture.completedFuture(float)
                }
            } ?: source.reset().let {
                val action = source.nextBlock()
                Bacikal.Action { frame ->
                    frame.run(action).thenApply { it?.cfloat }
                }
            }
        }
    }

    fun float(def: Float? = null, display: String = "float"): LiveData<Float> {
        return floatOrNull().map { it ?: def ?: error("No $display selected.") }
    }

    fun doubleOrNull(): LiveData<Double?> {
        return LiveData {
            source.mark()
            source.nextToken().toDoubleOrNull()?.let { double ->
                Bacikal.Action {
                    CompletableFuture.completedFuture(double)
                }
            } ?: source.reset().let {
                val action = source.nextBlock()
                Bacikal.Action { frame ->
                    frame.run(action).thenApply { it?.cdouble }
                }
            }
        }
    }

    fun double(def: Double? = null, display: String = "double"): LiveData<Double> {
        return doubleOrNull().map { it ?: def ?: error("No $display selected.") }
    }

    fun literal(): LiveData<String> = readerOf { it.nextToken() }

    fun textOrNull(): LiveData<String?> = frameBy { it?.toString() }
    fun text(def: String? = null, display: String = "text"): LiveData<String> {
        return frameBy { it?.toString() ?: def ?: error("No $display selected.") }
    }

    fun multilineOrNull(): LiveData<List<String>?> = frameBy { it?.liveStringList }
    fun multiline(def: List<String>? = null, display: String = "multiline text"): LiveData<List<String>> {
        return frameBy { it?.liveStringList ?: def ?: error("No $display selected.") }
    }

    fun stringOrList(): LiveData<Any> = frameBy {
        when (it) {
            is String -> it
            is Array<*> -> {
                it.mapNotNull { el -> el?.toString() }
            }
            is Collection<*> -> {
                it.mapNotNull { el -> el?.toString() }
            }
            else -> error("No text or list selected.")
        }
    }

    fun vectorOrNull(): LiveData<Vector?> = frameBy { it?.liveVector }
    fun vector(def: Vector? = null, display: String = "vector"): LiveData<Vector> {
        return frameBy { it?.liveVector ?: def ?: error("No $display selected.") }
    }

    fun locationOrNull(): LiveData<Location?> = frameBy { it?.liveLocation }
    fun location(def: Location? = null, display: String = "location"): LiveData<Location> {
        return frameBy { it?.liveLocation ?: def ?: error("No $display selected.") }
    }

    fun colorOrNull(): LiveData<Color?> = frameBy { it?.liveColor }
    fun color(def: Color? = null, display: String = "color"): LiveData<Color> {
        return frameBy { it?.liveColor ?: def ?: error("No $display selected.") }
    }

    fun entityOrNull(): LiveData<Entity?> = frameBy { it?.liveEntity }
    fun entity(def: Entity? = null, display: String = "entity"): LiveData<Entity> {
        return frameBy { it?.liveEntity ?: def ?: error("No $display selected.") }
    }

    fun playerOrNull(): LiveData<Player?> = frameBy { it?.livePlayer }
    fun player(def: Player? = null, display: String = "player"): LiveData<Player> {
        return frameBy { it?.livePlayer ?: def ?: error("No $display selected.") }
    }

    fun itemOrNull(): LiveData<ItemStack?> = frameBy { it?.liveItemStack }
    fun item(def: ItemStack? = null, display: String = "itemStack"): LiveData<ItemStack> {
        return frameBy { it?.liveItemStack ?: def ?: error("No $display selected.") }
    }

    fun inventoryOrNull(): LiveData<Inventory?> = frameBy { it?.liveInventory }
    fun inventory(def: Inventory? = null, display: String = "itemStack"): LiveData<Inventory> {
        return frameBy { it?.liveInventory ?: def ?: error("No $display selected.") }
    }

    fun applyLiveData(vararg liveData: LiveData<*>) {

        var startIndex = -1
        var endIndex = liveData.lastIndex

        for ((index, it) in liveData.withIndex()) {
            if (it is LiveDataProxy<*>) {
                startIndex = index
                break
            }
            it.accept(reader = this)
        }

        if (startIndex < 0) return

        if (liveData.last() !is LiveDataProxy<*>) {
            // 处理特殊情况
            endIndex -= 1
        }

        while (peekToken().matches(argumentPrefixPattern)) {
            val prefix = nextToken().substring(1)
            for (index in startIndex..endIndex) {
                (liveData[index] as LiveDataProxy<*>).accept(prefix, reader = this)
            }
        }

        if (liveData.last() !is LiveDataProxy<*>) {
            liveData.last().accept(reader = this)
        }
    }

    /*
     * discrete(...)
     * 无参数
     * */

    fun <R> discrete(func: (ScriptFrame) -> R): Bacikal.Parser<R> {
        return Bacikal.Parser { CompletableFuture.completedFuture(func(it)) }
    }

    fun <R> discreteOf(func: () -> Bacikal.Action<R>): Bacikal.Parser<R> {
        return Bacikal.Parser { frame ->
            func().run(frame)
        }
    }

    /*
    * combine(...)
    * func 返回 R
    * */

    inline fun <P1, R> combine(
        p1: LiveData<P1>,
        crossinline func: ScriptFrame.(P1) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenApply { t1 -> func(frame, t1) }
        }
    }

    inline fun <P1, R> combineOf(
        p1: LiveData<P1>,
        crossinline func: ScriptFrame.(P1) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 -> func(frame, t1) }
        }
    }

    inline fun <P1, P2, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        crossinline func: ScriptFrame.(P1, P2) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2)
            }
        }
    }

    inline fun <P1, P2, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        crossinline func: ScriptFrame.(P1, P2) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2)
            }
        }
    }

    fun <P1, P2, P3, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        func: ScriptFrame.(P1, P2, P3) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3)
            }
        }
    }

    fun <P1, P2, P3, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        func: ScriptFrame.(P1, P2, P3) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3)
            }
        }
    }

    fun <P1, P2, P3, P4, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        func: ScriptFrame.(P1, P2, P3, P4) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4)
            }
        }
    }

    fun <P1, P2, P3, P4, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        func: ScriptFrame.(P1, P2, P3, P4) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        func: ScriptFrame.(P1, P2, P3, P4, P5) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        func: ScriptFrame.(P1, P2, P3, P4, P5) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        p9: LiveData<P9>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8, P9) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8, p9)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame),
                p9.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        p9: LiveData<P9>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8, P9) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8, p9)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame),
                p9.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        p9: LiveData<P9>,
        p10: LiveData<P10>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8, P9, P10) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame),
                p9.accept(frame),
                p10.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9, it.t10)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        p9: LiveData<P9>,
        p10: LiveData<P10>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8, P9, P10) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame),
                p9.accept(frame),
                p10.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9, it.t10)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        p9: LiveData<P9>,
        p10: LiveData<P10>,
        p11: LiveData<P11>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11) -> R
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame),
                p9.accept(frame),
                p10.accept(frame),
                p11.accept(frame)
            ).thenApply {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9, it.t10, it.t11)
            }
        }
    }

    fun <P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        p6: LiveData<P6>,
        p7: LiveData<P7>,
        p8: LiveData<P8>,
        p9: LiveData<P9>,
        p10: LiveData<P10>,
        p11: LiveData<P11>,
        func: ScriptFrame.(P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11) -> CompletableFuture<R>
    ): Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)
        return Bacikal.Parser { frame ->
            applicative(
                p1.accept(frame),
                p2.accept(frame),
                p3.accept(frame),
                p4.accept(frame),
                p5.accept(frame),
                p6.accept(frame),
                p7.accept(frame),
                p8.accept(frame),
                p9.accept(frame),
                p10.accept(frame),
                p11.accept(frame)
            ).thenCompose {
                func(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9, it.t10, it.t11)
            }
        }
    }

}