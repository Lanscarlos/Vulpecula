package top.lanscarlos.vulpecula.module.bacikal.parser

import kotlinx.metadata.Flag
import kotlinx.metadata.internal.metadata.jvm.deserialization.JvmProtoBufUtil
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.function.info
import taboolib.common.reflect.hasAnnotation
import taboolib.library.kether.*
import taboolib.library.reflex.AnalyseMode
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.applicative.Applicative
import top.lanscarlos.vulpecula.common.applicative.ApplicativeRegistry
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Expected
import top.lanscarlos.vulpecula.module.bacikal.annotation.Optional
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.LinkedList
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-11-20 11:11
 */
@RuntimeDependency(
    "!org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0",
    test = "!kotlinx.metadata.jvm.KotlinClassMetadata",
    relocate = ["!kotlin.", "!kotlin210.", "!kotlinx.metadata.", "!kotlinx.metadata060."],
    transitive = false
)
class BacikalActionParser(javaClass: Class<*>, val instance: BacikalActionResolver) : QuestActionParser {

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
        if (!BacikalActionResolver::class.java.isAssignableFrom(javaClass)) {
            // 未实现 BacikalActionResolver 接口
            error("BacikalActionParser#init >> ${javaClass.name} does not implement BacikalActionResolver.")
        }
        if (javaClass.declaredMethods.count { it.name == "resolve" } != 1) {
            // 仅允许定义一个 resolve 方法
            error("BacikalActionParser#init >> ${javaClass.name} has more than one resolve method.")
        }

        // 获取函数
        standardFunction = javaClass.declaredMethods.find { it.name == "resolve" }!!
        defaultFunction = javaClass.declaredMethods.find { it.name == "resolve\$default" }

        // 使用 Reflex 解析参数
        val reflexClass = ReflexClass.of(javaClass, AnalyseMode.ASM_ONLY)
        val reflexMethod = reflexClass.structure.methods.find { it.name == "resolve" }!!
        val reflexParameters = reflexMethod.parameter

        // 使用 ProtoBuf 解析元信息
        val metadata = reflexClass.structure.annotations.find { it.source.simpleName == "Metadata" }!!
        val data1 = BacikalRegistry.metadata[javaClass.name]!!
        val data2 = metadata.list<String>("d2").toTypedArray()
        val (resolver, pbClass) = JvmProtoBufUtil.readClassDataFrom(data1, data2)
        val pbFunction = pbClass.functionList.find { resolver.getString(it.name) == "resolve" }!!
        val pbParameters = pbFunction.valueParameterList

        // 解析参数
        parameters = standardFunction.parameters.mapIndexed { index, parameter ->
            val name = resolver.getString(pbParameters[index].name)
            val isNullable = reflexParameters[index].isAnnotationPresent(org.jetbrains.annotations.Nullable::class.java)
            val hasDefaultValue = Flag.ValueParameter.DECLARES_DEFAULT_VALUE.invoke(pbParameters[index].flags)
            Parameter(index, name, isNullable, hasDefaultValue, parameter)
        }

        // 检查返回值
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
            error("BacikalActionParser#invoke >> Parameter count mismatch.")
        }

        // 检查参数非空性
        for (index in parameters.indices) {
            if (parameters[index] == null && !this.parameters[index].isNullable) {
                // 参数非空性检查失败
                error("BacikalActionParser#invoke >> Parameter ${this.parameters[index].type.name} at index $index is not nullable.")
            }
        }

        if (defaultFunction != null && mask != 0) {
            // 参数缺省
            try {
                return defaultFunction.invoke(instance, instance, *parameters, mask, null)
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
//            info("BacikalActionParser#execute >> Invoke standard function. parameters: ${parameters.joinToString()}")
            return standardFunction.invoke(instance, *parameters)
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
                info("BacikalActionParser#resolve >> Additional parameter found. $prefix >> ${reader.peekToken()}")
                val (index, parameter) = additional[prefix] ?: error("BacikalActionParser#resolve >> Unknown additional parameter $prefix")
                actions[index] = parameter.parse(reader)
            }

            // 将未初始化的参数替换为缺省值
            for (i in actions.indices) {
                if (actions[i] is UninitializedAction) {
                    actions[i] = DefaultAction(i, parameters[i].type)
                }
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

    inner class Parameter(val index: Int, val name: String, val isNullable: Boolean, val hasDefaultValue: Boolean, source: java.lang.reflect.Parameter) {

        val type: Class<*> = source.type

        val prefix: Array<String>

        val modifier: Int

        init {
            info("Parameter index=$index; name=$name; type=${type.simpleName}; nullable=$isNullable; hasDefaultValue=$hasDefaultValue")
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
            if (type == BacikalFrame::class.java) {
                return FrameAction
            }
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
                    // 附加参数前面前缀在本函数调用前已经验证过了
                    reader.readAction()
                }
                else -> error("BacikalActionParser\$Parameter#accept >> Unsupported modifier $modifier for parameter $name of action ${instance.id}")
            }

//            val applicative = when (type) {
//                Boolean::class.java -> BooleanApplicative
//                Int::class.java -> IntApplicative
//                else -> error("BacikalActionParser\$Parameter#accept >> Unsupported parameter type ${type.name}")
//            }
            val applicative = ApplicativeRegistry.getApplicative(type)
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
            val parameters = process(actions, frame).exceptionally { ex ->
                ex.printStackTrace()
                throw ex
            }
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

    private fun process(queue: Array<BacikalAction<*>>, frame: BacikalFrame): CompletableFuture<out List<Any?>> {
        if (queue.isEmpty()) {
            return CompletableFuture.completedFuture(emptyList())
        }

        if (queue.size == 1) {
            val action = queue[0]
            val future = action.execute(frame)
            return future.handle { result, ex ->
                if (ex != null) {
                    // 中断队列
                    throw ex
                }
                listOf(result)
            }
        }

        // 构建顺序执行链
        return queue.fold(CompletableFuture.completedFuture<LinkedList<Any?>>(LinkedList())) { acc, action ->
            acc.thenCompose { results ->
                action.execute(frame).handle { result, ex ->
                    if (ex != null) {
                        // 中断队列
                        throw ex
                    }
                    results.add(result)
                    results
                }
            }
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
     * 帧对象提供
     * */
    object FrameAction : BacikalAction<BacikalFrame> {
        override fun execute(frame: BacikalFrame): CompletableFuture<BacikalFrame> {
            return CompletableFuture.completedFuture(frame)
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
    class ApplicativeAction<T: Any>(val source: ParsedAction<*>, val applicative: Applicative<T>) : BacikalAction<T> {
        override fun execute(frame: BacikalFrame): CompletableFuture<T> {
            return frame.runAction(source).thenApply(applicative::convertOrNull)
        }
    }

}