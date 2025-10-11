package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property
import top.lanscarlos.vulpecula.module.bacikal.extension.Extension
import top.lanscarlos.vulpecula.module.bacikal.extension.NativeExtension
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import top.lanscarlos.vulpecula.module.bacikal.parser.ExceptionalActionParser
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import java.io.File
import java.lang.reflect.ParameterizedType

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal
 *
 * @author Lanscarlos
 * @since 2025/6/17
 */
@Awake(LifeCycle.LOAD)
object BacikalScanner : ClassVisitor(5) {

    /**
     * 扫描类式语句解析器或属性
     * */
    override fun visitStart(owner: ReflexClass) {
        visitClass(owner, NativeExtension)
    }

    internal fun visitClass(owner: ReflexClass, extension: Extension) {
        when {
            owner.hasAnnotation(Parser::class.java) -> {
                // 语句
                val parser = try {
                    buildClassActionParser(owner, extension)
                } catch (ex: Exception) {
                    console().error { ex.localizedMessage }
                    ExceptionalActionParser(ex, extension)
                }
                BacikalRegistry.registerActionParser(parser)
            }
            owner.hasAnnotation(Property::class.java) -> {
                // 属性
                registerBacikalProperty(owner, extension)
            }
        }
    }

    private fun buildClassActionParser(owner: ReflexClass, extension: Extension): ClassActionParser {
        if (!owner.hasInterface(ClassActionResolver::class.java)) {
            error("Cannot register class ${owner.name} without ClassActionResolver interface.")
        }
        val clazz = owner.toClass()
        val annotation = owner.toClass().getAnnotation(Parser::class.java)
        val parser = ClassActionParser(
            annotation.id,
            annotation.name,
            annotation.aliases,
            annotation.namespace,
            annotation.description,
            clazz,
            extension
        )
        return parser
    }

    private fun registerBacikalProperty(owner: ReflexClass, extension: Extension) {
        if (!owner.hasInterface(BacikalProperty::class.java)) {
            error("Cannot register class ${owner.name} without BacikalProperty interface.")
        }
        val javaClass = owner.toClass()
        val annotation = javaClass.getAnnotation(Property::class.java)
        val bind = annotation.bind.java
        val property = owner.getInstance() as BacikalProperty<*>
        BacikalRegistry.registerProperty(bind, property, extension)
    }

    /**
     * 获取类的泛型
     * */
    private fun getParameterizedType(clazz: Class<*>): Class<*> {
        return when (val it = (clazz.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)) {
            is Class<*> -> it
            is ParameterizedType -> it.rawType as Class<*>
            else -> throw NullPointerException()
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

}