package top.lanscarlos.vulpecula.bacikal.parser

import taboolib.common.reflect.hasAnnotation
import taboolib.library.kether.*
import top.lanscarlos.vulpecula.applicative.Applicative
import top.lanscarlos.vulpecula.applicative.BooleanApplicative
import top.lanscarlos.vulpecula.applicative.IntApplicative
import top.lanscarlos.vulpecula.bacikal.BacikalActionResolver
import top.lanscarlos.vulpecula.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.bacikal.annotation.Expected
import top.lanscarlos.vulpecula.bacikal.annotation.Optional
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-11-20 11:11
 */
class BacikalActionParser(owner: Class<*>) : QuestActionParser {

    companion object {
        const val MODIFIER_NONE = 0
        const val MODIFIER_EXPECTED = 1
        const val MODIFIER_OPTIONAL = 2
        const val MODIFIER_ADDITIONAL = 4
    }

    /**
     * 标准函数, 参数不缺省时调用
     * */
    private val standardFunction: Method

    /**
     * 缺省函数, 参数缺省时调用
     * */
    private val defaultFunction: Method?

    /**
     * 参数列表
     * */
    private val parameters: List<Parameter>

    /**
     * 是否使用 CompletableFuture 作为返回值
     * */
    private val useFutureReturn: Boolean

    init {
        // 类结构验证
        if (!BacikalActionResolver::class.java.isAssignableFrom(owner)) {
            // 未实现 BacikalActionResolver 接口
            error("BacikalActionParser#init >> ${owner.name} does not implement BacikalActionResolver.")
        }
        if (owner.declaredMethods.count { it.name == "resolve" } != 1) {
            // 仅允许定义一个 resolve 方法
            error("BacikalActionParser#init >> ${owner.name} has more than one resolve method.")
        }

        // 获取函数
        standardFunction = owner.declaredMethods.find { it.name == "resolve" }!!
        defaultFunction = owner.declaredMethods.find { it.name == "resolve\$default" }

        // 解析参数
        parameters = standardFunction.parameters.mapIndexed(::Parameter)

        //
        useFutureReturn = standardFunction.returnType == CompletableFuture::class.java
    }

    /**
     * 执行函数
     *
     * @param mask 缺省参数掩码
     * @param parameters 参数列表
     * */
    fun invoke(mask: Int, parameters: Array<Any?>): Any? {
        if (parameters.size != this.parameters.size) {
            // 参数数量不匹配
            error("BacikalActionParser#execute >> Parameter count mismatch.")
        }

        // 检查参数非空性
        for (index in parameters.indices) {
            if (parameters[index] == null && !this.parameters[index].isNullable) {
                // 参数非空性检查失败
                error("BacikalActionParser#execute >> Parameter ${this.parameters[index].type.name} at index $index is not nullable.")
            }
        }

        if (defaultFunction != null && mask != 0) {
            // 参数缺省
            try {
                return defaultFunction.invoke(null, this@BacikalActionParser, *parameters, mask, null)
            } catch (e: Exception) {
                if (e is InvocationTargetException) {
                    e.targetException.printStackTrace()
                } else {
                    e.printStackTrace()
                }
            }
            return null
        }

        // 无参数缺省
        try {
            return standardFunction.invoke(null, *parameters)
        } catch (e: Exception) {
            if (e is InvocationTargetException) {
                e.targetException.printStackTrace()
            } else {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * 解析语句
     * */
    override fun <T : Any?> resolve(source: QuestReader): QuestAction<T> {
        // 编译为可执行的 QuestAction
        val actions: Array<BacikalAction<*>> = Array(parameters.size) { UninitializedAction }
        val reader = DefaultReader(source)
        val additional = mutableMapOf<String, Pair<Int, Parameter>>()
        var breakIndex = 0 // 断点索引

        // 解析参数并提取附加参数
        for ((index, parameter) in parameters.withIndex()) {
            when {
                parameter.modifier == MODIFIER_ADDITIONAL -> {
                    // 提取附加参数
                    parameter.prefix.forEach { additional[it] = index to parameter }
                }
                additional.isEmpty() -> {
                    // 非附加参数, 并且当前未检测到附加参数，正常解析参数
                    actions[index] = parameter.parse(reader)
                }
                else -> {
                    // 已检索到附加参数，当前为第一个非附加参数，设置断点
                    breakIndex = index
                    break
                }
            }
        }

        // 解析附加参数
        if (additional.isNotEmpty()) {
            val regex = "-\\D+".toRegex()
            while (reader.peekToken().matches(regex)) {
                val prefix = reader.readToken().substring(1)
                val (index, parameter) = additional[prefix] ?: error("BacikalActionParser#resolve >> Unknown additional parameter $prefix")
                actions[index] = parameter.parse(reader)
            }

            // 读取剩余非附加参数
            if (breakIndex > 0) {
                // 已定位断点，跳过断点前的语句;
                // 断点必然大于零，因为前面至少有一个附加参数
                for (index in breakIndex until parameters.size) {
                    actions[index] = parameters[index].parse(reader)
                }
            }
        }

        return QuestActionResolver(actions)
    }

    class Parameter(val index: Int, source: java.lang.reflect.Parameter) {

        val type: Class<*> = source.type

        val isNullable: Boolean = source.hasAnnotation(org.jetbrains.annotations.Nullable::class.java)

        val prefix: Array<String>

        val modifier: Int

        init {
            prefix = when {
                source.hasAnnotation(Expected::class.java) -> {
                    modifier = MODIFIER_EXPECTED
                    source.getAnnotation(Expected::class.java).prefix
                }
                source.hasAnnotation(Optional::class.java) -> {
                    modifier = MODIFIER_OPTIONAL
                    source.getAnnotation(Optional::class.java).prefix
                }
                source.hasAnnotation(Additional::class.java) -> {
                    modifier = MODIFIER_ADDITIONAL
                    source.getAnnotation(Additional::class.java).prefix
                }
                else -> {
                    modifier = MODIFIER_NONE
                    emptyArray<String>()
                }
            }
        }

        fun parse(reader: BacikalReader): BacikalAction<*> {
            val action: ParsedAction<*> = when (modifier) {
                MODIFIER_NONE -> {
                    reader.readAction()
                }
                MODIFIER_EXPECTED -> {
                    if (prefix.isNotEmpty()) {
                        reader.expectToken(*prefix)
                    }
                    reader.readAction()
                }
                MODIFIER_OPTIONAL -> {
                    if (!reader.hasToken(*prefix)) {
                        // 缺省参数
                        return DefaultAction(index, type)
                    }
                    reader.readAction()
                }
                MODIFIER_ADDITIONAL -> {
                    if (!reader.hasToken(*prefix)) {
                        // 缺省参数
                        return DefaultAction(index, type)
                    }
                    reader.readAction()
                }
                else -> error("BacikalActionParser\$Parameter#accept >> Unsupported modifier $modifier")
            }

            val applicative = when (type) {
                Boolean::class.java -> BooleanApplicative
                Int::class.java -> IntApplicative
                else -> error("BacikalActionParser\$Parameter#accept >> Unsupported parameter type ${type.name}")
            }

            return ApplicativeAction(action, applicative)
        }

    }

    inner class QuestActionResolver<T>(val actions: Array<BacikalAction<*>>) : QuestAction<T>() {

        // 计算缺省掩码值
        val mask: Int = actions.fold(0) { acc, action ->
            if (action !is DefaultAction) {
                return@fold acc
            }
            acc or action.mask
        }

        @Suppress("UNCHECKED_CAST")
        override fun process(source: QuestContext.Frame): CompletableFuture<T> {
            val frame = DefaultFrame(source)
            val queue = actions.map { it.execute(frame) }
            val parameters = process(queue)
            return if (useFutureReturn) {
                parameters.thenCompose {
                    invoke(mask, it.toTypedArray()) as CompletableFuture<T>
                }
            } else {
                parameters.thenApply {
                    invoke(mask, it.toTypedArray()) as T
                }
            }
        }

    }

    private fun process(queue: List<CompletableFuture<*>>): CompletableFuture<List<Any?>> {
        if (queue.isEmpty()) {
            return CompletableFuture.completedFuture(emptyList())
        }

        if (queue.size == 1) {
            val future = queue[0]
            if (future.isDone) {
                return CompletableFuture.completedFuture(listOf(future.getNow(null)))
            }
            return future.thenApply { listOf(it) }
        }

        return CompletableFuture.allOf(*queue.toTypedArray()).thenApply {
            queue.map(CompletableFuture<*>::join)
        }
    }

    /**
     * 未初始化参数, 作占位用
     * */
    object UninitializedAction : BacikalAction<Unit> {
        override fun execute(frame: BacikalFrame): CompletableFuture<Unit> {
            return CompletableFuture.completedFuture(Unit)
        }
    }

    /**
     * 缺省参数
     *
     * @param index 缺省位置
     * */
    class DefaultAction(index: Int, type: Class<*>) : BacikalAction<Any?> {

        // 计算缺省掩码值
        val mask: Int = 1 shl index

        // 对基本类型生成缺省值
        val defaultValue = when (type) {
            Boolean::class.java -> false
            Short::class.java -> 0.toShort()
            Int::class.java -> 0
            Long::class.java -> 0L
            Float::class.java -> 0.0f
            Double::class.java -> 0.0
            else -> null
        }

        override fun execute(frame: BacikalFrame): CompletableFuture<Any?> {
            return CompletableFuture.completedFuture(defaultValue)
        }
    }

    // 转变参数
    class ApplicativeAction<T>(val source: ParsedAction<*>, val applicative: Applicative<T>) : BacikalAction<T> {
        override fun execute(frame: BacikalFrame): CompletableFuture<T> {
            return frame.runAction(source).thenApply(applicative::apply)
        }
    }

}