package top.lanscarlos.vulpecula.module.bacikal.parser

import taboolib.common.platform.function.console
import taboolib.library.reflex.AnalyseMode
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.lang.Lang
import java.lang.reflect.Constructor

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2025/6/28
 */
class ClassActionConstructor(javaClass: Class<*>) {

    private val instance: ClassActionResolver?

    private val constructor: Constructor<*>?

    init {
        // 类结构验证
        require(ClassActionResolver::class.java.isAssignableFrom(javaClass)) {
            // 未实现 ClassActionResolver 接口
            Lang.BACIKAL_INVALID_CLASS_IMPLEMENT.asText(console(), javaClass.name, ClassActionResolver::class.java.simpleName)
        }

        val reflexClass = ReflexClass.of(javaClass, AnalyseMode.ASM_ONLY)
        instance = reflexClass.getInstance() as? ClassActionResolver
        if (instance == null) {
            // 约束构造函数
            require(javaClass.declaredConstructors.size == 1) {
                Lang.BACIKAL_INVALID_CONSTRUCTORS_SIZE.asText(console())

            }
            constructor = javaClass.declaredConstructors.single()
            constructor.isAccessible = true
            val parameters = constructor.parameters
            require(parameters.size <= 1) {
                Lang.BACIKAL_INVALID_CONSTRUCTOR_PARAMETER.asText(console(), BacikalReader::class.java.simpleName)
            }
            require(parameters.size == 0 || parameters[0].type == BacikalReader::class.java) {
                Lang.BACIKAL_INVALID_CONSTRUCTOR_PARAMETER.asText(console(), BacikalReader::class.java.simpleName)
            }
        } else {
            constructor = null
        }
    }

    fun getOrNewInstance(reader: BacikalReader): ClassActionResolver {
        if (instance != null) {
            return instance
        }
        val instance = if (constructor!!.parameters.size == 0) {
            constructor.newInstance()
        } else {
            constructor.newInstance(reader)
        }
        return instance as ClassActionResolver
    }

}