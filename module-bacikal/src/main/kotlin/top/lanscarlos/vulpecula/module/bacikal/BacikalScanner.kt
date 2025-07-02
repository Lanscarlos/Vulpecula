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
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver
import top.lanscarlos.vulpecula.module.bacikal.parser.ExceptionalActionParser
import java.io.File

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
                if (!owner.hasAnnotation(BacikalParser::class.java)) {
                    continue
                }
                val parser = try {
                    buildClassActionParser(owner, source)
                } catch (ex: Exception) {
                    console().error { ex.localizedMessage }
                    ExceptionalActionParser(ex, source)
                }
                BacikalRegistry.registerActionParser(parser)
            }
        }
    }

    /**
     * 扫描类式语句解析器
     * */
    override fun visitStart(owner: ReflexClass) {
        if (!owner.hasAnnotation(BacikalParser::class.java)) {
            return
        }
        val parser = try {
            buildClassActionParser(owner, BuiltInActionSource)
        } catch (ex: Exception) {
            console().error { ex.localizedMessage }
            ExceptionalActionParser(ex, BuiltInActionSource)
        }
        BacikalRegistry.registerActionParser(parser)
    }

    private fun buildClassActionParser(owner: ReflexClass, source: ActionSource): ClassActionParser {
        if (!owner.hasInterface(ClassActionResolver::class.java)) {
            error("Cannot register class ${owner.name} without BacikalActionResolver interface.")
        }
        val clazz = owner.toClass()
        val annotation = owner.toClass().getAnnotation(BacikalParser::class.java)
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

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

}