package top.lanscarlos.vulpecula.bacikal.parser

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.ParsedAction
import top.lanscarlos.vulpecula.bacikal.Maturation
import java.awt.Color
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-21 00:41
 */
interface BacikalContext {

    /**
     * 读取器
     * */
    val reader: BacikalReader

    /**
     * 可空参数
     * */
    fun <T> nullable(then: BacikalSeed<T>): BacikalSeed<T?>

    /**
     * 二元组
     * */
    fun <T, R> pair(first: BacikalSeed<T>, second: BacikalSeed<R>): BacikalSeed<Pair<T, R>>

    /**
     * 三元组
     * */
    fun <T, R, S> triple(first: BacikalSeed<T>, second: BacikalSeed<R>, third: BacikalSeed<S>): BacikalSeed<Triple<T, R, S>>

    /**
     * 期望参数
     * */
    fun <T> expect(vararg expect: String, then: BacikalSeed<T>): BacikalSeed<T>

    /**
     * 可选参数
     * */
    fun <T> optional(vararg expect: String, then: BacikalSeed<T>): BacikalSeed<T?>

    /**
     * 可选参数
     * */
    fun <T> optional(vararg expect: String, then: BacikalSeed<T>, def: T): BacikalSeed<T>

    /**
     * 附加参数
     * */
    fun <T> addition(vararg prefix: String, then: BacikalSeed<T>): BacikalSeed<T?>

    /**
     * 附加参数
     * */
    fun <T> addition(vararg prefix: String, then: BacikalSeed<T>, def: T): BacikalSeed<T>

    fun token(): BacikalSeed<String>

    fun action(): BacikalSeed<ParsedAction<*>>

    fun any(): BacikalSeed<Any?>

    fun boolean(def: Boolean? = null, warning: String = "No boolean selected."): BacikalSeed<Boolean>

    fun short(def: Short? = null, warning: String = "No short selected."): BacikalSeed<Short>

    fun int(def: Int? = null, warning: String = "No int selected."): BacikalSeed<Int>

    fun long(def: Long? = null, warning: String = "No long selected."): BacikalSeed<Long>

    fun float(def: Float? = null, warning: String = "No float selected."): BacikalSeed<Float>

    fun double(def: Double? = null, warning: String = "No double selected."): BacikalSeed<Double>

    fun text(def: String? = null, warning: String = "No text selected."): BacikalSeed<String>

    fun multiline(def: List<String>? = null, warning: String = "No multiline selected."): BacikalSeed<List<String>>

    fun color(def: Color? = null, warning: String = "No color selected."): BacikalSeed<Color>

    fun entity(def: Entity? = null, warning: String = "No entity selected."): BacikalSeed<Entity>

    fun inventory(def: Inventory? = null, warning: String = "No inventory selected."): BacikalSeed<Inventory>

    fun item(def: ItemStack? = null, warning: String = "No item selected."): BacikalSeed<ItemStack>

    fun location(def: Location? = null, warning: String = "No location selected."): BacikalSeed<Location>

    fun player(def: Player? = null, warning: String = "No player selected."): BacikalSeed<Player>

    fun playerList(def: List<Player>? = null, warning: String = "No player list selected."): BacikalSeed<List<Player>>

    fun vector(def: Vector? = null, warning: String = "No vector selected."): BacikalSeed<Vector>

    fun <R> fructus(
        func: Maturation.M0<BacikalFrame, R>
    ): BacikalFruit<R>

    fun <S1, R> fructus(
        s1: BacikalSeed<S1>,
        func: Maturation.M1<BacikalFrame, S1, R>
    ): BacikalFruit<R>

    fun <S1, S2, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        func: Maturation.M2<BacikalFrame, S1, S2, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        func: Maturation.M3<BacikalFrame, S1, S2, S3, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        func: Maturation.M4<BacikalFrame, S1, S2, S3, S4, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        func: Maturation.M5<BacikalFrame, S1, S2, S3, S4, S5, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        func: Maturation.M6<BacikalFrame, S1, S2, S3, S4, S5, S6, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        func: Maturation.M7<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        func: Maturation.M8<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, R> fructus(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        func: Maturation.M9<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, R> fructus(
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
        func: Maturation.M10<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, R> fructus(
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
        func: Maturation.M11<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, R> fructus(
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
        func: Maturation.M12<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, R> fructus(
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
        func: Maturation.M13<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, R> fructus(
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
        func: Maturation.M14<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, R> fructus(
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
        func: Maturation.M15<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, R>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, R> fructus(
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
        func: Maturation.M16<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, R>
    ): BacikalFruit<R>

    fun <R> fructusFuture(
        func: Maturation.M0<BacikalFrame, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, R> fructusFuture(
        s1: BacikalSeed<S1>,
        func: Maturation.M1<BacikalFrame, S1, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        func: Maturation.M2<BacikalFrame, S1, S2, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        func: Maturation.M3<BacikalFrame, S1, S2, S3, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        func: Maturation.M4<BacikalFrame, S1, S2, S3, S4, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        func: Maturation.M5<BacikalFrame, S1, S2, S3, S4, S5, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        func: Maturation.M6<BacikalFrame, S1, S2, S3, S4, S5, S6, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        func: Maturation.M7<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        func: Maturation.M8<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, R> fructusFuture(
        s1: BacikalSeed<S1>,
        s2: BacikalSeed<S2>,
        s3: BacikalSeed<S3>,
        s4: BacikalSeed<S4>,
        s5: BacikalSeed<S5>,
        s6: BacikalSeed<S6>,
        s7: BacikalSeed<S7>,
        s8: BacikalSeed<S8>,
        s9: BacikalSeed<S9>,
        func: Maturation.M9<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, R> fructusFuture(
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
        func: Maturation.M10<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, R> fructusFuture(
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
        func: Maturation.M11<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, R> fructusFuture(
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
        func: Maturation.M12<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, R> fructusFuture(
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
        func: Maturation.M13<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, R> fructusFuture(
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
        func: Maturation.M14<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, R> fructusFuture(
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
        func: Maturation.M15<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, CompletableFuture<R>>
    ): BacikalFruit<R>

    fun <S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, R> fructusFuture(
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
        func: Maturation.M16<BacikalFrame, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10, S11, S12, S13, S14, S15, S16, CompletableFuture<R>>
    ): BacikalFruit<R>
}