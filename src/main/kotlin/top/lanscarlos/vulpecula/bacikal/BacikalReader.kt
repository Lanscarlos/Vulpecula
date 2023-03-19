package top.lanscarlos.vulpecula.bacikal

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.common5.cbool
import taboolib.common5.cdouble
import taboolib.common5.cint
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.run
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.frame
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveColor
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveEntity
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveItemStack
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveLocation
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.livePlayer
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveStringList
import top.lanscarlos.vulpecula.bacikal.LiveData.Companion.liveVector
import top.lanscarlos.vulpecula.kether.action.ActionBlock
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

    fun token(): String {
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
            ParsedAction(ActionBlock(ActionBlock.readBlock(reader = source)))
        } else {
            source.nextParsedAction()
        }
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

    fun <T> trim(vararg expect: String, then: LiveData<T>) : LiveData<T> {
        return then.trim(*expect)
    }

    fun <T> expect(vararg expect: String, then: LiveData<T>) : LiveData<T> {
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
        return LiveData {
            val action = readAction()
            Bacikal.Action { CompletableFuture.completedFuture(action) }
        }
    }

    fun any(): LiveData<Any?> = frame { it }

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
                Bacikal.Action { CompletableFuture.completedFuture(int) }
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

    fun doubleOrNull(): LiveData<Double?> {
        return LiveData {
            source.mark()
            source.nextToken().toDoubleOrNull()?.let { double ->
                Bacikal.Action { CompletableFuture.completedFuture(double) }
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

    fun textOrNull(): LiveData<String?> = frame { it?.toString() }
    fun text(def: String? = null, display: String = "text"): LiveData<String> {
        return frame { it?.toString() ?: def ?: error("No $display selected.") }
    }

    fun multilineOrNull(): LiveData<List<String>?> = frame { it?.liveStringList }
    fun multiline(def: List<String>? = null, display: String = "multiline text"): LiveData<List<String>> {
        return frame { it?.liveStringList ?: def ?: error("No $display selected.") }
    }

    fun stringOrList(): LiveData<Any> = frame {
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

    fun vectorOrNull(): LiveData<Vector?> = frame { it?.liveVector }
    fun vector(def: Vector? = null, display: String = "vector"): LiveData<Vector> {
        return frame { it?.liveVector ?: def ?: error("No $display selected.") }
    }

    fun locationOrNull(): LiveData<Location?> = frame { it?.liveLocation }
    fun location(def: Location? = null, display: String = "location"): LiveData<Location> {
        return frame { it?.liveLocation ?: def ?: error("No $display selected.") }
    }

    fun colorOrNull(): LiveData<Color?> = frame { it?.liveColor }
    fun color(def: Color? = null, display: String = "color"): LiveData<Color> {
        return frame { it?.liveColor ?: def ?: error("No $display selected.") }
    }

    fun entityOrNull(): LiveData<Entity?> = frame { it?.liveEntity }
    fun entity(def: Entity? = null, display: String = "entity"): LiveData<Entity> {
        return frame { it?.liveEntity ?: def ?: error("No $display selected.") }
    }

    fun playerOrNull(): LiveData<Player?> = frame { it?.livePlayer }
    fun player(def: Player? = null, display: String = "player"): LiveData<Player> {
        return frame { it?.livePlayer ?: def ?: error("No $display selected.") }
    }

    fun itemOrNull(): LiveData<ItemStack?> = frame { it?.liveItemStack }
    fun item(def: ItemStack? = null, display: String = "itemStack"): LiveData<ItemStack> {
        return frame { it?.liveItemStack ?: def ?: error("No $display selected.") }
    }

    fun applyLiveData(vararg liveData: LiveData<*>) {
        var parametric = -1
        for ((index, it) in liveData.withIndex()) {
            if (it is LiveDataProxy<*>) {
                parametric = index
                break
            }
            it.accept(reader = this)
        }

        if (parametric < 0) return
        while (peekToken().matches(argumentPrefixPattern)) {
            val prefix = token().substring(1)
            for (index in parametric until liveData.size) {
                (liveData[index] as LiveDataProxy<*>).accept(prefix, this)
            }
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
    ) : Bacikal.Parser<R> {
        return combineOf(p1) { t1 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1))
            }
        }
    }

    inline fun <P1, P2, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        crossinline func: ScriptFrame.(P1, P2) -> R
    ) : Bacikal.Parser<R> {
        return combineOf(p1, p2) { t1, t2 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1, t2))
            }
        }
    }

    fun <P1, P2, P3, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        func: ScriptFrame.(P1, P2, P3) -> R
    ) : Bacikal.Parser<R> {
        return combineOf(p1, p2, p3) { t1, t2, t3 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1, t2, t3))
            }
        }
    }

    fun <P1, P2, P3, P4, R> combine(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        func: ScriptFrame.(P1, P2, P3, P4) -> R
    ) : Bacikal.Parser<R> {
        return combineOf(p1, p2, p3, p4) { t1, t2, t3, t4 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1, t2, t3, t4))
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
    ) : Bacikal.Parser<R> {
        return combineOf(p1, p2, p3, p4, p5) { t1, t2, t3, t4, t5 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1, t2, t3, t4, t5))
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
    ) : Bacikal.Parser<R> {
        return combineOf(p1, p2, p3, p4, p5, p6) { t1, t2, t3, t4, t5, t6 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1, t2, t3, t4, t5, t6))
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
    ) : Bacikal.Parser<R> {
        return combineOf(p1, p2, p3, p4, p5, p6, p7) { t1, t2, t3, t4, t5, t6, t7 ->
            Bacikal.Action { frame ->
                CompletableFuture.completedFuture(func(frame, t1, t2, t3, t4, t5, t6, t7))
            }
        }
    }

    /*
    * combineOf(...)
    * func 返回 Action<R>
    * */

    inline fun <P1, R> combineOf(
        p1: LiveData<P1>,
        crossinline func: (P1) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                func(t1).run(frame)
            }
        }
    }

    inline fun <P1, P2, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        crossinline func: (P1, P2) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1, p2)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                p2.accept(frame).thenCompose { t2 ->
                    func(t1, t2).run(frame)
                }
            }
        }
    }

    fun <P1, P2, P3, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        func: (P1, P2, P3) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                p2.accept(frame).thenCompose { t2 ->
                    p3.accept(frame).thenCompose { t3 ->
                        func(t1, t2, t3).run(frame)
                    }
                }
            }
        }
    }

    fun <P1, P2, P3, P4, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        func: (P1, P2, P3, P4) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                p2.accept(frame).thenCompose { t2 ->
                    p3.accept(frame).thenCompose { t3 ->
                        p4.accept(frame).thenCompose { t4 ->
                            func(t1, t2, t3, t4).run(frame)
                        }
                    }
                }
            }
        }
    }

    fun <P1, P2, P3, P4, P5, R> combineOf(
        p1: LiveData<P1>,
        p2: LiveData<P2>,
        p3: LiveData<P3>,
        p4: LiveData<P4>,
        p5: LiveData<P5>,
        func: (P1, P2, P3, P4, P5) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                p2.accept(frame).thenCompose { t2 ->
                    p3.accept(frame).thenCompose { t3 ->
                        p4.accept(frame).thenCompose { t4 ->
                            p5.accept(frame).thenCompose { t5 ->
                                func(t1, t2, t3, t4, t5).run(frame)
                            }
                        }
                    }
                }
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
        func: (P1, P2, P3, P4, P5, P6) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                p2.accept(frame).thenCompose { t2 ->
                    p3.accept(frame).thenCompose { t3 ->
                        p4.accept(frame).thenCompose { t4 ->
                            p5.accept(frame).thenCompose { t5 ->
                                p6.accept(frame).thenCompose { t6 ->
                                    func(t1, t2, t3, t4, t5, t6).run(frame)
                                }
                            }
                        }
                    }
                }
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
        func: (P1, P2, P3, P4, P5, P6, P7) -> Bacikal.Action<R>
    ) : Bacikal.Parser<R> {
        applyLiveData(p1, p2, p3, p4, p5, p6, p7)
        return Bacikal.Parser { frame ->
            p1.accept(frame).thenCompose { t1 ->
                p2.accept(frame).thenCompose { t2 ->
                    p3.accept(frame).thenCompose { t3 ->
                        p4.accept(frame).thenCompose { t4 ->
                            p5.accept(frame).thenCompose { t5 ->
                                p6.accept(frame).thenCompose { t6 ->
                                    p7.accept(frame).thenCompose { t7 ->
                                        func(t1, t2, t3, t4, t5, t6, t7).run(frame)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}