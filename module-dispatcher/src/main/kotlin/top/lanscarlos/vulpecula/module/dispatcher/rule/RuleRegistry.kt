package top.lanscarlos.vulpecula.module.dispatcher.rule

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.dispatcher.DispatcherRule
import java.lang.reflect.ParameterizedType

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/4 15:16
 */
@Awake(LifeCycle.LOAD)
object RuleRegistry : ClassVisitor() {

    private val registry: HashMap<String, ReflexClass> = linkedMapOf()

    fun get(clazz: ReflexClass): ReflexClass {
        return registry[clazz.name!!] ?: ReflexClass.of(GenericEventRule::class.java)
    }

    override fun visitStart(owner: ReflexClass) {
        val clazz = owner.toClass()
        if (clazz.`package`.name != this.javaClass.`package`.name) {
            // 包路径不对
            return
        }
        if (clazz == GenericEventRule::class.java) {
            return
        }
        if (!DispatcherRule::class.java.isAssignableFrom(clazz)) {
            warning("Class ${owner.name} does not implement DispatcherRule<T>")
            return
        }
        clazz.isInterface
        if (owner.structure.isInterface || owner.structure.isAbstract) {
            // 非实现类
            return
        }

        // 获取事件类型
        val type = when (val it = (owner.toClass().genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)) {
            is Class<*> -> it
            is ParameterizedType -> it.rawType as Class<*>
            else -> {
                warning("Property \"${owner.toClass().name}\" must have a generic type.")
                return
            }
        }

        info("Registering Rule ${type.simpleName} for ${owner.name}")
        registry[type.name] = owner
    }

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

}