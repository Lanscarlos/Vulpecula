package top.lanscarlos.vulpecula.module.bacikal.parser

import kotlinx.metadata.Flag
import kotlinx.metadata.internal.metadata.jvm.deserialization.JvmProtoBufUtil
import taboolib.common.env.RuntimeDependency
import taboolib.library.reflex.AnalyseMode
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.core.utils.asLang
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
@RuntimeDependency(
    "!org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0",
    test = "!kotlinx.metadata.jvm.KotlinClassMetadata",
    relocate = ["!kotlin.", "!kotlin210.", "!kotlinx.metadata.", "!kotlinx.metadata060."],
    transitive = false
)
class ClassActionFunction(
    metadata: Array<String>,
    val resolver: ClassActionResolver
) {

    /**
     * 标准函数, 参数不缺省时调用
     * */
    private val standardMethod: Method

    /**
     * 缺省函数, 参数缺省时调用
     * */
    private val defaultMethod: Method?

    /**
     * 参数列表
     * */
    internal val parameters: List<ClassActionParameter>

    /**
     * 是否使用 CompletableFuture 作为返回值
     * */
    internal val isUseFutureReturn: Boolean

    init {
        val javaClass = resolver::class.java

        // 类结构验证
        require(ClassActionResolver::class.java.isAssignableFrom(javaClass)) {
            // 未实现 ClassActionResolver 接口
            asLang("module-bacikal-exception-invalid-class-action-resolver", javaClass.name, ClassActionResolver::class.java.simpleName)
        }
        require(javaClass.declaredMethods.count { it.name == "resolve" } == 1) {
            // 仅允许定义一个 resolve 方法
            asLang("module-bacikal-exception-invalid-resolve-function", javaClass.name, "resolve")
        }

        // 获取函数
        standardMethod = javaClass.declaredMethods.find { it.name == "resolve" }!!
        defaultMethod = javaClass.declaredMethods.find { it.name == "resolve\$default" }

        // 检查返回值
        isUseFutureReturn = standardMethod.returnType == CompletableFuture::class.java

        // 使用 Reflex 解析参数
        val reflexClass = ReflexClass.of(javaClass, AnalyseMode.ASM_ONLY)
        val reflexMethod = reflexClass.structure.methods.find { it.name == "resolve" }!!
        val reflexParameters = reflexMethod.parameter

        // 使用 ProtoBuf 解析元信息
        val data = reflexClass.structure.annotations.find { it.source.simpleName == "Metadata" }!!.list<String>("d2").toTypedArray()
        val (resolver, pbClass) = JvmProtoBufUtil.readClassDataFrom(metadata, data)
        val pbFunction = pbClass.functionList.find { resolver.getString(it.name) == "resolve" }!!
        val pbParameters = pbFunction.valueParameterList

        // 解析参数
        parameters = standardMethod.parameters.mapIndexed { index, parameter ->
            val name = resolver.getString(pbParameters[index].name)
            val isNullable = reflexParameters[index].isAnnotationPresent(org.jetbrains.annotations.Nullable::class.java)
            val hasDefaultValue = Flag.ValueParameter.DECLARES_DEFAULT_VALUE.invoke(pbParameters[index].flags)
            ClassActionParameter(index, name, isNullable, hasDefaultValue, parameter)
        }
    }

    /**
     * 执行函数
     *
     * @param mask 缺省参数掩码
     * @param parameters 参数列表
     * */
    fun invoke(mask: Int, arguments: Array<Any?>): Any? {
        require(parameters.size == arguments.size) {
            // 参数数量不匹配
            "BacikalActionParser#invoke >> Argument count mismatch."
        }

        // 检查参数非空性
        for (parameter in parameters) {
            require(parameter.isNullable || arguments[parameter.index] != null) {
                // 参数非空性检查失败
                asLang("module-bacikal-exception-invalid-argument", parameter.index, parameter.name)
            }
        }

        if (mask == 0 || defaultMethod == null) {
            // 无缺省实参
            try {
                return standardMethod.invoke(resolver, *arguments)
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }

        // 实参缺省
        try {
            return defaultMethod.invoke(resolver, resolver, *arguments, mask, null)
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }


}