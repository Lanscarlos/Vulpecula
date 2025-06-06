package top.lanscarlos.vulpecula.dispatcher.rule

import org.bukkit.event.Event
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.dispatcher.Rule
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
        if (owner.toClass().`package`.name != this.javaClass.`package`.name) {
            // 包路径不对
            return
        }
        if (owner.toClass() == GenericEventRule::class.java) {
            return
        }
        if (owner.structure.isInterface || owner.structure.isAbstract) {
            // 非实现类
            return
        }
        if (!owner.hasInterface(Rule::class.java)) {
            warning("Class ${owner.name} does not implement Rule<T>")
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