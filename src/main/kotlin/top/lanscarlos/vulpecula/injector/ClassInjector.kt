package top.lanscarlos.vulpecula.injector

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.library.reflex.ClassField
import taboolib.library.reflex.ClassMethod
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.action.effect.pattern.CanvasPattern
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.injector
 *
 * @author Lanscarlos
 * @since 2022-11-13 14:15
 */
abstract class ClassInjector(
    val priority: Int = 0,
    val packageName: String? = null
) {

    open fun visit(field: ClassField, clazz: Class<*>, supplier: Supplier<*>?) {}

    open fun visit(method: ClassMethod, clazz: Class<*>, supplier: Supplier<*>?) {}

    open fun visitEnd(clazz: Class<*>, supplier: Supplier<*>?) {}

    open fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {}

    @Awake(LifeCycle.LOAD)
    companion object : ClassVisitor(0) {

        private val injectors: List<ClassInjector> = listOf(
            CanvasPattern.Companion,
            KetherRegistry
        )

        override fun getLifeCycle() = LifeCycle.LOAD

        override fun visit(field: ClassField, clazz: Class<*>, supplier: Supplier<*>?) {
            injectors.forEach {
                if (it.packageName != null && !clazz.packageName.startsWith(it.packageName)) return@forEach
                it.visit(field, clazz, supplier)
            }
        }

        override fun visit(method: ClassMethod, clazz: Class<*>, supplier: Supplier<*>?) {
            injectors.forEach {
                if (it.packageName != null && !clazz.packageName.startsWith(it.packageName)) return@forEach
                it.visit(method, clazz, supplier)
            }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            injectors.forEach {
                if (it.packageName != null && !clazz.packageName.startsWith(it.packageName)) return@forEach
                it.visitStart(clazz, supplier)
            }
        }

        override fun visitEnd(clazz: Class<*>, supplier: Supplier<*>?) {
            injectors.forEach {
                if (it.packageName != null && !clazz.packageName.startsWith(it.packageName)) return@forEach
                it.visitEnd(clazz, supplier)
            }
        }
    }
}