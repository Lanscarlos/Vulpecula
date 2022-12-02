package top.lanscarlos.vulpecula.internal

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ClassMethod
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.action.canvas.pattern.CanvasPattern
import top.lanscarlos.vulpecula.kether.action.entity.ActionEntity
import top.lanscarlos.vulpecula.kether.action.item.ActionItemStack
import top.lanscarlos.vulpecula.kether.action.location.ActionLocation
import top.lanscarlos.vulpecula.kether.action.target.ActionTarget
import top.lanscarlos.vulpecula.kether.action.vector.ActionVector
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.injector
 *
 * @author Lanscarlos
 * @since 2022-11-13 14:15
 */
abstract class ClassInjector(
    val packageName: String
) {

//    open fun visit(field: ClassField, clazz: Class<*>, supplier: Supplier<*>?) {}

    open fun visit(method: ClassMethod, clazz: Class<*>, supplier: Supplier<*>?) {}

//    open fun visitEnd(clazz: Class<*>, supplier: Supplier<*>?) {}

    open fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {}

    @Awake(LifeCycle.LOAD)
    object KetherClassInjector : ClassVisitor(0) {

        private val ketherRegistry = KetherRegistry
        private val injectors = mutableListOf<ClassInjector>()

        override fun getLifeCycle() = LifeCycle.LOAD

        override fun visit(method: ClassMethod, clazz: Class<*>, supplier: Supplier<*>?) {
            if (!clazz.packageName.startsWith(ketherRegistry.packageName)) return
            ketherRegistry.visit(method, clazz, supplier)

            injectors.forEach {
                if (!clazz.packageName.startsWith(it.packageName)) return@forEach
                it.visit(method, clazz, supplier)
            }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!clazz.packageName.startsWith(ketherRegistry.packageName)) return
            ketherRegistry.visitStart(clazz, supplier)

            injectors.forEach {
                if (!clazz.packageName.startsWith(it.packageName)) return@forEach
                it.visitStart(clazz, supplier)
            }
        }
    }
}