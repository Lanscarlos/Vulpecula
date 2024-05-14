package top.lanscarlos.vulpecula.bacikal.parser

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
import taboolib.library.kether.QuestContext
import taboolib.library.kether.QuestReader
import top.lanscarlos.vulpecula.applicative.AbstractApplicative
import top.lanscarlos.vulpecula.applicative.CollectionApplicative.Companion.collection
import top.lanscarlos.vulpecula.applicative.ColorApplicative.Companion.applicativeColor
import top.lanscarlos.vulpecula.applicative.EntityApplicative.Companion.applicativeEntity
import top.lanscarlos.vulpecula.applicative.InventoryApplicative.Companion.applicativeInventory
import top.lanscarlos.vulpecula.applicative.ItemStackApplicative.Companion.applicativeItemStack
import top.lanscarlos.vulpecula.applicative.LocationApplicative.Companion.applicativeLocation
import top.lanscarlos.vulpecula.applicative.PlayerApplicative.Companion.applicativePlayer
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeBoolean
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeDouble
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeFloat
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeInt
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeLong
import top.lanscarlos.vulpecula.applicative.PrimitiveApplicative.applicativeShort
import top.lanscarlos.vulpecula.applicative.VectorApplicative.Companion.applicativeVector
import java.awt.Color
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-05-13 15:58
 */
abstract class BacikalActionParser : QuestActionParser {

    companion object {
        const val MODIFIER_NONE = 0
        const val MODIFIER_EXPECTED = 1
        const val MODIFIER_OPTIONAL = 2
        const val MODIFIER_ADDITIONAL = 4
    }

    annotation class Expected(val prefix: Array<String>)

    annotation class Optional(val prefix: Array<String>)

    annotation class Additional(val prefix: Array<String>)

    /**
     * 获取标准函数
     * */
    private val methodStandard = this::class.java.declaredMethods.find { it.name == "resolve" && it.returnType != QuestAction::class.java }
        ?: error("Cannot find resolve method in ${this::class.java.name}")

    /**
     * 获取模板函数
     * */
    private val methodDefault = this::class.java.declaredMethods.find { it.name == "resolve\$default" }

    /**
     * 结构树
     * */
    private val structure = methodStandard.parameters.map { parameter ->
        val isNullable = parameter.getAnnotation(org.jetbrains.annotations.Nullable::class.java) != null
        val expected = parameter.getAnnotation(Expected::class.java)
        val optional = parameter.getAnnotation(Optional::class.java)
        val additional = parameter.getAnnotation(Additional::class.java)
        when {
            expected != null -> Node(parameter.type, isNullable, false, MODIFIER_EXPECTED, expected.prefix)
            optional != null -> Node(parameter.type, isNullable, true, MODIFIER_OPTIONAL, optional.prefix)
            additional != null -> Node(parameter.type, isNullable, true, MODIFIER_ADDITIONAL, additional.prefix)
            else -> Node(parameter.type, isNullable, false, MODIFIER_NONE, arrayOf())
        }
    }

    /**
     * 是否使用了 CompletableFuture
     * */
    private val usingFuture = methodStandard.returnType == CompletableFuture::class.java

    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        // 生成种子
        val seeds = structure.map { it.buildSeed() }

        // 激活种子
        germinate(DefaultReader(reader), seeds)

        // 返回动作
        return Action(seeds)
    }

    /**
     * 激活种子
     * */
    private fun germinate(reader: BacikalReader, seeds: List<BacikalSeed<*>>) {
        if (seeds.isEmpty()) {
            return
        }

        val arguments = mutableListOf<AdditionalSeed<*>>()
        var breakpoint = 0

        for ((index, it) in seeds.withIndex()) {
            if (it is AdditionalSeed<*>) {
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
            outer@ while (arguments.isNotEmpty() && reader.peekToken().matches(DefaultContext.PATTERN_ARGUMENT_PREFIX)) {
                val prefix = reader.readToken().substring(1)
                val iterator = arguments.iterator()
                inner@ while (iterator.hasNext()) {
                    val it = iterator.next()
                    if (it.accept(prefix, reader)) {
                        iterator.remove()
                        break@inner
                    }
                }
            }

            // 读取剩余语句
            if (breakpoint > 0) {
                // 已定位断点，跳过断点前的语句；（断点必须大于零，因为前面必须至少有一个附加参数）
                for (index in breakpoint until seeds.size) {
                    seeds[index].accept(reader)
                }
            }
        }

        // 没有附加参数时，所有语句都已被读取，此时不应该有剩余语句
    }

    /**
     * 处理 CompletableFuture 队列
     * */
    private fun process(queue: List<CompletableFuture<*>>): CompletableFuture<List<Any?>> {
        val result = CompletableFuture<List<Any?>>()

        if (queue.isEmpty()) {
            // 队列为空
            result.complete(listOf())
        } else if (queue.size == 1) {
            // 队列仅有一个
            val future = queue.first()
            if (future.isDone) {
                result.complete(listOf(future.getNow(null)))
            } else {
                future.thenAccept { result.complete(listOf(it)) }
            }
        } else {
            // 队列有多个
            val counter = AtomicInteger(0)
            for (it in queue) {
                if (it.isDone) {
                    val count = counter.incrementAndGet()

                    // 判断 futures 是否全部执行完毕
                    if (!result.isDone && count >= queue.size) {
                        result.complete(queue.map { it.getNow(null) })
                        return result
                    }
                } else {
                    it.thenRun {
                        val count = counter.incrementAndGet()

                        // 判断 futures 是否全部执行完毕
                        if (!result.isDone && count >= queue.size) {
                            result.complete(queue.map { it.getNow(null) })
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * 执行函数
     * */
    fun invoke(parameters: List<Any?>): Any? {
        if (parameters.size != this@BacikalActionParser.structure.size) {
            error("wrong number of arguments: ${parameters.size}/${this@BacikalActionParser.structure.size}")
        }
        if (methodDefault != null) {
            var mask = 0
            val params = parameters.mapIndexed { index, value ->
                val node = structure[index]
                if (value == null) {
                    // 空值处理
                    if (node.isOptional) {
                        // 计算掩码 采用默认值
                        mask = mask or (1 shl index)
                    } else if (!node.isNullable) {
                        // 非空
                        error("missing required argument at index: $index")
                    }
                }
                value ?: when (this@BacikalActionParser.structure[index].type) {
                    Boolean::class.java -> false
                    Short::class.java -> 0.toShort()
                    Int::class.java -> 0
                    Long::class.java -> 0L
                    Float::class.java -> 0.0f
                    Double::class.java -> 0.0
                    else -> null
                }
            }
            try {
                return methodDefault.invoke(null, this@BacikalActionParser, *params.toTypedArray(), mask, null)
            } catch (e: Exception) {
                if (e is InvocationTargetException) {
                    e.targetException.printStackTrace()
                } else {
                    e.printStackTrace()
                }
            }
        } else {
            val params = parameters.mapIndexed { index, value ->
                val node = structure[index]
                if (value == null && !node.isNullable) {
                    // 非空
                    error("missing required argument at index: $index")
                }
                value ?: when (this@BacikalActionParser.structure[index].type) {
                    Boolean::class.java -> false
                    Short::class.java -> 0.toShort()
                    Int::class.java -> 0
                    Long::class.java -> 0L
                    Float::class.java -> 0.0f
                    Double::class.java -> 0.0
                    else -> null
                }
            }
            try {
                return methodStandard.invoke(this@BacikalActionParser, *params.toTypedArray())
            } catch (e: Exception) {
                if (e is InvocationTargetException) {
                    e.targetException.printStackTrace()
                } else {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    private class Node(val type: Class<*>, val isNullable: Boolean, val isOptional: Boolean, val modifier: Int, val prefix: Array<String>) {

        fun buildSeed(): BacikalSeed<*> {
            val seed = buildSeed(type)
            return when (modifier) {
                MODIFIER_EXPECTED -> ExpectedSeed(seed, prefix)
                MODIFIER_OPTIONAL -> OptionalSeed(seed, prefix)
                MODIFIER_ADDITIONAL -> AdditionalSeed(seed, prefix)
                else -> seed
            }
        }

        private fun buildSeed(type: Class<*>): BacikalSeed<*> {
            return when (type) {
                Boolean::class.java -> BooleanSeed()
                Short::class.java -> ShortSeed()
                Int::class.java -> IntSeed()
                Long::class.java -> LongSeed()
                Float::class.java -> FloatSeed()
                Double::class.java -> DoubleSeed()
                String::class.java -> StringSeed()
                List::class.java -> ListSeed()
                Color::class.java -> ColorSeed()
                Entity::class.java -> EntitySeed()
                Player::class.java -> PlayerSeed()
                Inventory::class.java -> InventorySeed()
                ItemStack::class.java -> ItemStackSeed()
                Location::class.java -> LocationSeed()
                Vector::class.java -> VectorSeed()
                Pair::class.java -> {
                    val first = type.typeParameters[0].genericDeclaration as Class<*>
                    val second = type.typeParameters[1].genericDeclaration as Class<*>
                    PairSeed(buildSeed(first), buildSeed(second))
                }
                Triple::class.java -> {
                    val first = type.typeParameters[0].genericDeclaration as Class<*>
                    val second = type.typeParameters[1].genericDeclaration as Class<*>
                    val third = type.typeParameters[2].genericDeclaration as Class<*>
                    TripleSeed(buildSeed(first), buildSeed(second), buildSeed(third))
                }
                else -> error("Unsupported type ${type.name}")
            }
        }
    }

    private inner class Action<T>(val seeds: List<BacikalSeed<*>>) : QuestAction<T>() {

        @Suppress("UNCHECKED_CAST")
        override fun process(arg0: QuestContext.Frame): CompletableFuture<T> {
            val frame = DefaultFrame(arg0)
            val queue = seeds.map { it.accept(frame) }

            return if (usingFuture) {
                process(queue).thenCompose { parameters ->
                    invoke(parameters) as CompletableFuture<T>
                }
            } else {
                process(queue).thenApply { parameters ->
                    invoke(parameters) as T
                }
            }
        }

    }

    private class PairSeed<S1, S2>(val first: BacikalSeed<S1>, val second: BacikalSeed<S2>) : BacikalSeed<Pair<S1, S2>?> {

        override val isAccepted: Boolean
            get() = first.isAccepted && second.isAccepted

        override fun accept(reader: BacikalReader) {
            first.accept(reader)
            second.accept(reader)
        }

        override fun accept(frame: BacikalFrame): CompletableFuture<Pair<S1, S2>?> {
            return first.accept(frame).thenCompose { s1 ->
                second.accept(frame).thenApply { s2 ->
                    s1 to s2
                }
            }
        }
    }

    private class TripleSeed<S1, S2, S3>(val first: BacikalSeed<S1>, val second: BacikalSeed<S2>, val third: BacikalSeed<S3>) : BacikalSeed<Triple<S1, S2, S3>?> {

        override val isAccepted: Boolean
            get() = first.isAccepted && second.isAccepted && third.isAccepted

        override fun accept(reader: BacikalReader) {
            first.accept(reader)
            second.accept(reader)
            third.accept(reader)
        }

        override fun accept(frame: BacikalFrame): CompletableFuture<Triple<S1, S2, S3>?> {
            return first.accept(frame).thenCompose { s1 ->
                second.accept(frame).thenCompose { s2 ->
                    third.accept(frame).thenApply { s3 ->
                        Triple(s1, s2, s3)
                    }
                }
            }
        }
    }

    private class BooleanSeed : AbstractSeed<Boolean?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Boolean? {
            return value?.applicativeBoolean()?.getValue()
        }
    }

    private class ShortSeed : AbstractSeed<Short?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Short? {
            return value?.applicativeShort()?.getValue()
        }
    }

    private class IntSeed : AbstractSeed<Int?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Int? {
            return value?.applicativeInt()?.getValue()
        }
    }

    private class LongSeed : AbstractSeed<Long?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Long? {
            return value?.applicativeLong()?.getValue()
        }
    }

    private class FloatSeed : AbstractSeed<Float?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Float? {
            return value?.applicativeFloat()?.getValue()
        }
    }

    private class DoubleSeed : AbstractSeed<Double?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Double? {
            return value?.applicativeDouble()?.getValue()
        }
    }

    private class StringSeed : AbstractSeed<String?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): String? {
            return value?.toString()
        }
    }

    private class ListSeed : AbstractSeed<List<String>?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): List<String>? {
            if (value == null) {
                return null
            }
            return object : AbstractApplicative<String>(value) {
                override fun transfer(source: Any, def: String?): String {
                    return source.toString()
                }
            }.collection().getValue()?.toList()
        }
    }

    private class ColorSeed : AbstractSeed<Color?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Color? {
            return value?.applicativeColor()?.getValue()
        }
    }

    private class EntitySeed : AbstractSeed<Entity?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Entity? {
            return value?.applicativeEntity()?.getValue()
        }
    }

    private class PlayerSeed : AbstractSeed<Player?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Player? {
            return value?.applicativePlayer()?.getValue()
        }
    }

    private class InventorySeed : AbstractSeed<Inventory?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Inventory? {
            return value?.applicativeInventory()?.getValue()
        }
    }

    private class ItemStackSeed : AbstractSeed<ItemStack?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): ItemStack? {
            return value?.applicativeItemStack()?.getValue()
        }
    }

    private class LocationSeed : AbstractSeed<Location?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Location? {
            return value?.applicativeLocation()?.getValue()
        }
    }

    private class VectorSeed : AbstractSeed<Vector?>() {
        override fun resolve(frame: BacikalFrame, value: Any?): Vector? {
            return value?.applicativeVector()?.getValue()
        }
    }

}