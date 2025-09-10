package top.lanscarlos.vulpecula.module.bacikal

import taboolib.common.ClassAppender
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.io.getClasses
import taboolib.common.io.getResources
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.common.platform.function.releaseResourceFolder
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.bacikal.action.ActionSource
import top.lanscarlos.vulpecula.module.bacikal.action.BuiltInActionSource
import top.lanscarlos.vulpecula.module.bacikal.action.ExternalActionSource
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.annotation.Property
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

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 6, runnable = ::scanActionExtension)
        registerLifeCycleTask(LifeCycle.LOAD, 6, runnable = ::scanPropertyExtension)
    }

    /**
     * 扫描拓展属性包
     * */
    private fun scanPropertyExtension() {
        val folder = File(getDataFolder(), "property")
        if (!folder.exists()) {
            releaseResourceFolder("property")
        }
        for (file in folder.listFiles() ?: emptyArray<File>()) {
            if (!file.exists() || !file.isFile || !file.canRead() || file.extension != "jar") {
                continue
            }

            // 载入包体
            ClassAppender.addPath(file.toPath(), false, false)

            val classes = file.toURI().toURL().getClasses()
            val source = ExternalActionSource(classes, file.toURI().toURL().getResources())

            // 遍历 class 对象
            for (owner in classes.values) {
                visitClass(owner, source)
            }
        }
    }

    /**
     * 扫描拓展语句包
     * */
    private fun scanActionExtension() {
        val folder = File(getDataFolder(), "action")
        if (!folder.exists()) {
            releaseResourceFolder("action")
        }
        for (file in folder.listFiles() ?: emptyArray<File>()) {
            if (!file.exists() || !file.isFile || !file.canRead() || file.extension != "jar") {
                continue
            }

            // 载入包体
            ClassAppender.addPath(file.toPath(), false, false)

            val classes = file.toURI().toURL().getClasses()
            val source = ExternalActionSource(classes, file.toURI().toURL().getResources())

            // 遍历 class 对象
            for (owner in classes.values) {
                visitClass(owner, source)
            }
        }
    }

    /**
     * 扫描类式语句解析器或属性
     * */
    override fun visitStart(owner: ReflexClass) {
        visitClass(owner, BuiltInActionSource)
    }

    private fun visitClass(owner: ReflexClass, source: ActionSource) {
        when {
            owner.hasAnnotation(Parser::class.java) -> {
                // 语句
                val parser = try {
                    buildClassActionParser(owner, source)
                } catch (ex: Exception) {
                    console().error { ex.localizedMessage }
                    ExceptionalActionParser(ex, source)
                }
                BacikalRegistry.registerActionParser(parser)
            }
            owner.hasAnnotation(Property::class.java) -> {
                // 属性
                registerBacikalProperty(owner, source)
            }
        }
    }

    private fun buildClassActionParser(owner: ReflexClass, source: ActionSource): ClassActionParser {
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
            source
        )
        return parser
    }

    private fun registerBacikalProperty(owner: ReflexClass, source: ActionSource) {
        if (!owner.hasInterface(BacikalProperty::class.java)) {
            error("Cannot register class ${owner.name} without BacikalProperty interface.")
        }
        val javaClass = owner.toClass()
        val annotation = javaClass.getAnnotation(Property::class.java)
        val bind = annotation.bind.java
        val property = owner.getInstance() as BacikalProperty<*>
        BacikalRegistry.registerProperty(bind, property, source)
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