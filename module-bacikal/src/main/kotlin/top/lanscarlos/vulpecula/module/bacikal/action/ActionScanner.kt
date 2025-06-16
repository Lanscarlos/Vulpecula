package top.lanscarlos.vulpecula.module.bacikal.action

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.function.info
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/6/16
 */
object ActionScanner : ClassVisitor(1) {

    override fun visitStart(owner: ReflexClass) {
        if (!owner.hasAnnotation(BacikalParser::class.java)) {
            return
        }
        info("Registering action ${owner.name}")
        for (annotation in owner.structure.annotations) {
            info("    - Reflex Annotation: ${annotation.source.name}")
        }
        for (annotation in owner.toClass().annotations) {
            info("    - Java Annotation: ${annotation.annotationClass.qualifiedName}")
        }
        ActionClassRegister.registerAction(owner)
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

}