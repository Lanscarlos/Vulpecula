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

    /**
     * name -> Rule Class 映射
     * */
    private val registry: HashMap<String, ReflexClass> = linkedMapOf()

    /**
     * name -> Event Class 映射
     * */
    private val mapping: HashMap<String, ReflexClass> = linkedMapOf()

    fun get(name: String): ReflexClass {
        return registry[name] ?: ReflexClass.of(GenericEventRule::class.java)
    }

    fun mapping(name: String): ReflexClass {
        return mapping[name] ?: error("No event mapping \"$name\" found.")
    }

    override fun visitStart(owner: ReflexClass) {
        val clazz = owner.toClass()
        if (clazz.`package`.name != this.javaClass.`package`.name) {
            // 包路径不对
            return
        }
        if (!DispatcherRule::class.java.isAssignableFrom(clazz)) {
            // 未继承接口
            return
        }
        if (!clazz.isAnnotationPresent(Rule::class.java)) {
            // 没有 Rule 注解
            return
        }
        if (clazz == GenericEventRule::class.java) {
            // 排除泛型专用类
            return
        }

        // 获取事件类型
        val type = when (val it = (clazz.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)) {
            is Class<*> -> it
            is ParameterizedType -> it.rawType as Class<*>
            else -> {
                warning("Property \"${clazz.name}\" must have a generic type.")
                return
            }
        }

        val annotation = clazz.getAnnotation(Rule::class.java)
        if (annotation.value.isNotBlank()) {
            // 自定义名称不为空
            val name = "@${annotation.value}"
            mapping[name] = ReflexClass.of(type)
            registry[name] = owner
            return
        }

        registry[type.name] = owner
    }

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

}