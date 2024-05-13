package top.lanscarlos.vulpecula.bacikal.parser

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.common.util.Location
import taboolib.common.util.Vector
import taboolib.library.kether.QuestAction
import taboolib.library.kether.QuestActionParser
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-05-13 15:58
 */
abstract class BacikalActionParser : QuestActionParser {

    abstract val name: String

    abstract val author: Array<String>

    annotation class Expected(val prefix: Array<String>)

    annotation class Optional(val prefix: Array<String>)

    annotation class Additional(val prefix: Array<String>)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> resolve(reader: QuestReader): QuestAction<T> {
        val method = this::class.declaredFunctions.find { it.name == "resolve" && it.returnType.classifier != QuestAction::class }
            ?: error("Cannot find resolve method in ${this::class.java.name}")

        val seeds = LinkedHashMap<KParameter, BacikalSeed<*>>()
        for (parameter in method.parameters) {
            if (parameter.name == null) {
                // 主参数
                seeds[parameter] = object : BacikalSeed<BacikalActionParser> {
                    override val isAccepted: Boolean = true
                    override fun accept(reader: BacikalReader) {}
                    override fun accept(frame: BacikalFrame): CompletableFuture<BacikalActionParser> {
                        return CompletableFuture.completedFuture(this@BacikalActionParser)
                    }
                }
                continue
            }

            // 生成种子
            val seed = when (parameter.type.classifier) {
                Boolean::class -> booleanSeed()
                Short::class -> shortSeed()
                Int::class -> intSeed()
                Long::class -> longSeed()
                Float::class -> floatSeed()
                Double::class -> doubleSeed()
                String::class -> stringSeed()
                List::class -> listSeed()
                Color::class -> colorSeed()
                Entity::class -> entitySeed()
                Player::class -> playerSeed()
                Inventory::class -> inventorySeed()
                ItemStack::class -> itemStackSeed()
                Location::class -> locationSeed()
                Vector::class -> vectorSeed()
                else -> error("Unsupported type ${parameter.type}")
            }

            // 包装种子
            val expected = parameter.findAnnotation<Expected>()
            val optional = parameter.findAnnotation<Optional>()
            val additional = parameter.findAnnotation<Additional>()
            seeds[parameter] = when {
                expected != null -> ExpectedSeed(seed, expected.prefix)
                optional != null -> OptionalSeed(seed, optional.prefix)
                additional != null -> AdditionalSeed(seed, additional.prefix)
                else -> seed
            }
        }

        // 激活种子
        germinate(DefaultReader(reader), seeds.values.toList())

        return BacikalFruit { frame ->
            // 生成种子队列
            val queue = seeds.mapValues { it.value.accept(frame) }

            // 处理种子队列
            process(queue).thenCompose { value ->
                // 生成参数
                val params = value.filter { it.value == null && it.key.isOptional }
                val result = method.callBy(params)

                if (method.returnType.classifier == CompletableFuture::class) {
                    // 返回值类型为 CompletableFuture
                    result as CompletableFuture<Any?>
                } else {
                    CompletableFuture.completedFuture(result)
                }
            }.thenApply {
                it as T
            }
        }
    }

    private fun booleanSeed(): BacikalSeed<Boolean?> {
        return object : AbstractSeed<Boolean?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Boolean? {
                return value?.applicativeBoolean()?.getValue()
            }
        }
    }

    private fun shortSeed(): BacikalSeed<Short?> {
        return object : AbstractSeed<Short?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Short? {
                return value?.applicativeShort()?.getValue()
            }
        }
    }

    private fun intSeed(): BacikalSeed<Int?> {
        return object : AbstractSeed<Int?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Int? {
                return value?.applicativeInt()?.getValue()
            }
        }
    }

    private fun longSeed(): BacikalSeed<Long?> {
        return object : AbstractSeed<Long?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Long? {
                return value?.applicativeLong()?.getValue()
            }
        }
    }

    private fun floatSeed(): BacikalSeed<Float?> {
        return object : AbstractSeed<Float?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Float? {
                return value?.applicativeFloat()?.getValue()
            }
        }
    }

    private fun doubleSeed(): BacikalSeed<Double?> {
        return object : AbstractSeed<Double?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Double? {
                return value?.applicativeDouble()?.getValue()
            }
        }
    }

    private fun stringSeed(): BacikalSeed<String?> {
        return object : AbstractSeed<String?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): String? {
                return value?.toString()
            }
        }
    }

    private fun listSeed(): BacikalSeed<List<String>?> {
        return object : AbstractSeed<List<String>?>() {
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
    }

    private fun colorSeed(): BacikalSeed<Color?> {
        return object : AbstractSeed<Color?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Color? {
                return value?.applicativeColor()?.getValue()
            }
        }
    }

    private fun entitySeed(): BacikalSeed<Entity?> {
        return object : AbstractSeed<Entity?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Entity? {
                return value?.applicativeEntity()?.getValue()
            }
        }
    }

    private fun playerSeed(): BacikalSeed<Player?> {
        return object : AbstractSeed<Player?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Player? {
                return value?.applicativePlayer()?.getValue()
            }
        }
    }

    private fun inventorySeed(): BacikalSeed<Inventory?> {
        return object : AbstractSeed<Inventory?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Inventory? {
                return value?.applicativeInventory()?.getValue()
            }
        }
    }

    private fun itemStackSeed(): BacikalSeed<ItemStack?> {
        return object : AbstractSeed<ItemStack?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): ItemStack? {
                return value?.applicativeItemStack()?.getValue()
            }
        }
    }

    private fun locationSeed(): BacikalSeed<Location?> {
        return object : AbstractSeed<Location?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Location? {
                return value?.applicativeLocation()?.getValue()
            }
        }
    }

    private fun vectorSeed(): BacikalSeed<Vector?> {
        return object : AbstractSeed<Vector?>() {
            override fun resolve(frame: BacikalFrame, value: Any?): Vector? {
                return value?.applicativeVector()?.getValue()
            }
        }
    }

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
            outer@while (arguments.isNotEmpty() && reader.peekToken().matches(DefaultContext.PATTERN_ARGUMENT_PREFIX)) {
                val prefix = reader.readToken().substring(1)
                val iterator = arguments.iterator()
                inner@while (iterator.hasNext()) {
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

    private fun process(queue: Map<KParameter, CompletableFuture<*>>): CompletableFuture<Map<KParameter, Any?>> {
        val result = CompletableFuture<Map<KParameter, Any?>>()

        if (queue.isEmpty()) {
            // 队列为空
            result.complete(emptyMap())
        } else if (queue.size == 1) {
            // 队列仅有一个
            val future = queue.entries.first()
            if (future.value.isDone) {
                val value = future.value.getNow(null)
                result.complete(mapOf(future.key to value))
            } else {
                future.value.thenAccept { result.complete(mapOf(future.key to it)) }
            }
        } else {
            // 队列有多个
            val counter = AtomicInteger(0)
            for (entry in queue) {
                if (entry.value.isDone) {
                    val count = counter.incrementAndGet()

                    // 判断 futures 是否全部执行完毕
                    if (!result.isDone && count >= queue.size) {
                        result.complete(queue.mapValues { it.value.getNow(null) })
                        return result
                    }
                } else {
                    entry.value.thenRun {
                        val count = counter.incrementAndGet()

                        // 判断 futures 是否全部执行完毕
                        if (!result.isDone && count >= queue.size) {
                            result.complete(queue.mapValues { it.value.getNow(null) })
                        }
                    }
                }
            }
        }

        return result
    }

}