package top.lanscarlos.vulpecula.bacikal

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.function.warning
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.ParsedAction
import taboolib.library.kether.QuestReader
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.expects
import top.lanscarlos.vulpecula.applicative.AbstractApplicative
import top.lanscarlos.vulpecula.applicative.CollectionApplicative.Companion.collection
import top.lanscarlos.vulpecula.applicative.ColorApplicative.Companion.applicativeColor
import top.lanscarlos.vulpecula.applicative.EntityApplicative.Companion.applicativeEntity
import top.lanscarlos.vulpecula.applicative.InventoryApplicative.Companion.applicativeInventory
import top.lanscarlos.vulpecula.applicative.ItemStackApplicative.Companion.applicativeItemStack
import top.lanscarlos.vulpecula.applicative.LocationApplicative.Companion.applicativeLocation
import top.lanscarlos.vulpecula.applicative.PlayerApplicative.Companion.applicativePlayer
import top.lanscarlos.vulpecula.applicative.PlayerListApplicative.Companion.applicativePlayerList
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeBoolean
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeDouble
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeFloat
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeShort
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeInt
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeLong
import top.lanscarlos.vulpecula.applicative.VectorApplicative.Companion.applicativeVector
import top.lanscarlos.vulpecula.bacikal.seed.*
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-21 11:59
 */
class DefaultBacikalContext(val source: QuestReader) : BacikalContext {

    override val reader: BacikalReader = Reader()

    override fun <T> nullable(then: BacikalSeed<T>): BacikalSeed<T?> {
        return NullableBacikalSeed(then)
    }

    override fun <T> expect(vararg expect: String, then: BacikalSeed<T>): BacikalSeed<T> {
        return ExpectedBacikalSeed(then, expect)
    }

    override fun <T> optional(vararg expect: String, then: BacikalSeed<T>): BacikalSeed<T?> {
        return OptionalBacikalSeed(nullable(then), expect, def = null)
    }

    override fun <T> optional(vararg expect: String, then: BacikalSeed<T>, def: T): BacikalSeed<T> {
        return OptionalBacikalSeed(then, expect, def)
    }

    override fun <T> argument(vararg prefix: String, then: BacikalSeed<T>): BacikalSeed<T?> {
        return ArgumentBacikalSeed(nullable(then), prefix, null)
    }

    override fun <T> argument(vararg prefix: String, then: BacikalSeed<T>, def: T): BacikalSeed<T> {
        return ArgumentBacikalSeed(then, prefix, def)
    }

    override fun token(): BacikalSeed<String> {
        return object : BacikalSeed<String> {
            lateinit var literal: String
            override var isAccepted = false

            override fun accept(reader: BacikalReader) {
                literal = reader.readToken()
                isAccepted = true
            }

            override fun accept(frame: ScriptFrame): CompletableFuture<String> {
                return CompletableFuture.completedFuture(literal)
            }
        }
    }

    override fun action(): BacikalSeed<ParsedAction<*>> {
        return object : BacikalSeed<ParsedAction<*>> {
            lateinit var action: ParsedAction<*>
            override var isAccepted = false

            override fun accept(reader: BacikalReader) {
                action = reader.readAction()
                isAccepted = true
            }

            override fun accept(frame: ScriptFrame): CompletableFuture<ParsedAction<*>> {
                return CompletableFuture.completedFuture(action)
            }
        }
    }

    override fun any(): BacikalSeed<Any?> {
        return DefaultBacikalSeed { _, source -> source }
    }

    override fun boolean(def: Boolean?, warning: String): BacikalSeed<Boolean> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeBoolean().getValue()
        }
    }

    override fun short(def: Short?, warning: String): BacikalSeed<Short> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeShort().getValue()
        }
    }

    override fun int(def: Int?, warning: String): BacikalSeed<Int> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeInt().getValue()
        }
    }

    override fun long(def: Long?, warning: String): BacikalSeed<Long> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeLong().getValue()
        }
    }

    override fun float(def: Float?, warning: String): BacikalSeed<Float> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeFloat().getValue()
        }
    }

    override fun double(def: Double?, warning: String): BacikalSeed<Double> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeDouble().getValue()
        }
    }

    override fun text(def: String?, warning: String): BacikalSeed<String> {
        return buildSeed(def, warning) { _, source ->
            source.toString()
        }
    }

    override fun multiline(def: List<String>?, warning: String): BacikalSeed<List<String>> {
        return buildSeed(def, warning) { _, source ->
            object : AbstractApplicative<String>(source) {
                override fun transfer(source: Any, def: String?): String {
                    return source.toString()
                }
            }.collection().getValue()?.toList()
        }
    }

    override fun color(def: Color?, warning: String): BacikalSeed<Color> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeColor().getValue()
        }
    }

    override fun entity(def: Entity?, warning: String): BacikalSeed<Entity> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeEntity().getValue()
        }
    }

    override fun inventory(def: Inventory?, warning: String): BacikalSeed<Inventory> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeInventory().getValue()
        }
    }

    override fun item(def: ItemStack?, warning: String): BacikalSeed<ItemStack> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeItemStack().getValue()
        }
    }

    override fun location(def: Location?, warning: String): BacikalSeed<Location> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeLocation().getValue()
        }
    }

    override fun player(def: Player?, warning: String): BacikalSeed<Player> {
        return buildSeed(def, warning) { _, source ->
            source.applicativePlayer().getValue()
        }
    }

    override fun playerList(def: List<Player>?, warning: String): BacikalSeed<List<Player>> {
        return buildSeed(def, warning) { _, source ->
            source.applicativePlayerList().getValue()
        }
    }

    override fun vector(def: Vector?, warning: String): BacikalSeed<Vector> {
        return buildSeed(def, warning) { _, source ->
            source.applicativeVector().getValue()
        }
    }

    private fun <T> buildSeed(def: T?, warning: String, func: (ScriptFrame, Any) -> T?): BacikalSeed<T> {
        return DefaultBacikalSeed { frame, source ->
            if (source != null) {
                func(frame, source) ?: def ?: error(warning)
            } else {
                def ?: error(warning)
            }
        }
    }

    private fun germinate(vararg seed: BacikalSeed<*>) {
        if (seed.isEmpty()) {
            return
        }

        val arguments = mutableListOf<ArgumentBacikalSeed<*>>()
        var breakpoint = 0

        for ((index, it) in seed.withIndex()) {
            if (it is ArgumentBacikalSeed<*>) {
                arguments.add(it)
            } else if (arguments.isEmpty()) {
                // 未检测到附加参数，正常解析语句
                it.accept(reader)
            } else {
                // 已检索到附加参数，当前为第一个非附加参数，设置断点
                breakpoint = index
                break
            }
        }

        if (arguments.isNotEmpty()) {
            // 读取附加参数
            while (reader.peekToken().matches(PATTERN_ARGUMENT_PREFIX)) {
                val prefix = reader.readToken().substring(1)
                for (it in arguments) {
                    it.accept(prefix, reader)
                }
            }

            // 读取剩余语句
            if (breakpoint > 0) {
                // 已定位断点，跳过断点前的语句；（断点必须大于零，因为前面必须至少有一个附加参数）
                for (index in breakpoint until seed.size) {
                    seed[index].accept(reader)
                }
            }
        }

        // 没有附加参数时，所有语句都已被读取，此时不应该有剩余语句
    }

    override fun <R> fructus(func: Maturation.M0<ScriptFrame, R>): BacikalFruit<R> {
        return BacikalFruit { frame ->
            CompletableFuture.completedFuture(func.apply(frame))
        }
    }

    override fun <S1, R> fructus(
        s1: BacikalSeed<S1>,
        func: Maturation.M1<ScriptFrame, S1, R>
    ): BacikalFruit<R> {
        germinate(s1)
        return BacikalFruit { frame ->
            s1.accept(frame).thenApply { t1 ->
                func.apply(frame, t1)
            }
        }
    }

    override fun <S1, S2, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        func: Maturation.M2<ScriptFrame, S1, S2, R>
    ): BacikalFruit<R> {
        germinate(s1, s2)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2)
            }
        }
    }

    override fun <S1, S2, S3, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        func: Maturation.M3<ScriptFrame, S1, S2, S3, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3)
            }
        }
    }

    override fun <S1, S2, S3, S4, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        func: Maturation.M4<ScriptFrame, S1, S2, S3, S4, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        func: Maturation.M5<ScriptFrame, S1, S2, S3, S4, S5, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        func: Maturation.M6<ScriptFrame, S1, S2, S3, S4, S5, S6, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        func: Maturation.M7<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        func: Maturation.M8<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        func: Maturation.M9<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        func: Maturation.M10<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame)
            ).thenApply {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9, it.t10)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        func: Maturation.M11<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame)
            ).thenApply {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        func: Maturation.M12<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame)
            ).thenApply {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        func: Maturation.M13<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame)
            ).thenApply {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        s14: BacikalSeed<S14>,
        func: Maturation.M14<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame),
                s14.accept(frame)
            ).thenApply {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13,
                    it.t14
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        s14: BacikalSeed<S14>,
        s15: BacikalSeed<S15>,
        func: Maturation.M15<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame),
                s14.accept(frame),
                s15.accept(frame)
            ).thenApply {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13,
                    it.t14,
                    it.t15
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        s14: BacikalSeed<S14>,
        s15: BacikalSeed<S15>,
        s16: BacikalSeed<S16>,
        func: Maturation.M16<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, R>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame),
                s14.accept(frame),
                s15.accept(frame),
                s16.accept(frame)
            ).thenApply {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13,
                    it.t14,
                    it.t15,
                    it.t16
                )
            }
        }
    }

    override fun <R> fructusFuture(func: Maturation.M0<ScriptFrame, CompletableFuture<R>>): BacikalFruit<R> {
        return BacikalFruit { frame ->
            func.apply(frame)
        }
    }

    override fun <S1, R> fructusFuture(
        s1: BacikalSeed<S1>,
        func: Maturation.M1<ScriptFrame, S1, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1)
        return BacikalFruit { frame ->
            s1.accept(frame).thenCompose { t1 ->
                func.apply(frame, t1)
            }
        }
    }

    override fun <S1, S2, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        func: Maturation.M2<ScriptFrame, S1, S2, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2)
            }
        }
    }

    override fun <S1, S2, S3, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        func: Maturation.M3<ScriptFrame, S1, S2, S3, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3)
            }
        }
    }

    override fun <S1, S2, S3, S4, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        func: Maturation.M4<ScriptFrame, S1, S2, S3, S4, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        func: Maturation.M5<ScriptFrame, S1, S2, S3, S4, S5, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        func: Maturation.M6<ScriptFrame, S1, S2, S3, S4, S5, S6, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        func: Maturation.M7<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        func: Maturation.M8<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        func: Maturation.M9<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        func: Maturation.M10<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame)
            ).thenCompose {
                func.apply(frame, it.t1, it.t2, it.t3, it.t4, it.t5, it.t6, it.t7, it.t8, it.t9, it.t10)
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        func: Maturation.M11<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame)
            ).thenCompose {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        func: Maturation.M12<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame)
            ).thenCompose {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        func: Maturation.M13<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame)
            ).thenCompose {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        s14: BacikalSeed<S14>,
        func: Maturation.M14<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame),
                s14.accept(frame)
            ).thenCompose {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13,
                    it.t14
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        s14: BacikalSeed<S14>,
        s15: BacikalSeed<S15>,
        func: Maturation.M15<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame),
                s14.accept(frame),
                s15.accept(frame)
            ).thenCompose {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13,
                    it.t14,
                    it.t15
                )
            }
        }
    }

    override fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        s10: BacikalSeed<S10>,
        s11: BacikalSeed<S11>,
        s12: BacikalSeed<S12>,
        s13: BacikalSeed<S13>,
        s14: BacikalSeed<S14>,
        s15: BacikalSeed<S15>,
        s16: BacikalSeed<S16>,
        func: Maturation.M16<ScriptFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, CompletableFuture<R>>
    ): BacikalFruit<R> {
        germinate(s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16)
        return BacikalFruit { frame ->
            combineFuture(
                s1.accept(frame),
                s2.accept(frame),
                s3.accept(frame),
                s4.accept(frame),
                s5.accept(frame),
                s6.accept(frame),
                s7.accept(frame),
                s8.accept(frame),
                s9.accept(frame),
                s10.accept(frame),
                s11.accept(frame),
                s12.accept(frame),
                s13.accept(frame),
                s14.accept(frame),
                s15.accept(frame),
                s16.accept(frame)
            ).thenCompose {
                func.apply(
                    frame,
                    it.t1,
                    it.t2,
                    it.t3,
                    it.t4,
                    it.t5,
                    it.t6,
                    it.t7,
                    it.t8,
                    it.t9,
                    it.t10,
                    it.t11,
                    it.t12,
                    it.t13,
                    it.t14,
                    it.t15,
                    it.t16
                )
            }
        }
    }

    companion object {
        val PATTERN_ARGUMENT_PREFIX = "-\\D+".toRegex()
    }

    inner class Reader : BacikalReader {

        private val marker: MutableList<Int> = mutableListOf()

        override val index: Int
            get() = source.index

        override fun readToken(): String {
            return source.nextToken()
        }

        override fun readAction(): ParsedAction<*> {
            return if (hasToken("{")) {
                // 语句块
                source.nextParsedAction()
            } else {
                source.nextParsedAction()
            }
        }

        override fun readActionList(): List<ParsedAction<*>> {
            return if (hasToken("[")) {
                mutableListOf<ParsedAction<*>>().also {
                    while (!hasToken("]")) {
                        it += readAction()
                    }
                }
            } else {
                emptyList()
            }
        }

        override fun peekToken(): String {
            source.mark()
            val token = source.nextToken()
            source.reset()
            return token
        }

        override fun expectToken(vararg expect: String) {
            source.expects(*expect)
        }

        override fun hasToken(vararg expect: String): Boolean {
            source.mark()
            val token = source.nextToken()
            return if (token in expect) {
                true
            } else {
                source.reset()
                false
            }
        }

        override fun mark(): Int {
            source.mark()
            marker += source.mark
            return source.mark
        }

        override fun rollback(offset: Int) {
            if (offset <= 0) {
                return
            }
            if (marker.size <= offset) {
                warning("Out of marker range. ${marker.size} <= $offset.")
                return
            }
            if (offset == 1) {
                // 常规回滚
                source.reset()
                marker.removeLast()
            } else {
                // 回滚到指定位置
                val index = marker[marker.lastIndex - offset]
                try {
                    source.setProperty("index", index)
                    // 移除多余的标记
                    repeat(offset) {
                        marker.removeLast()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    warning("Failed to rollback to index $index.")
                }
            }
        }
    }
}