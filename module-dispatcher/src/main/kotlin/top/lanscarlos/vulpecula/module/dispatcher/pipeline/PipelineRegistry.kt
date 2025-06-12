package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import org.bukkit.event.Event
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.module.dispatcher.EventPipeline
import top.lanscarlos.vulpecula.module.dispatcher.rule.Rule
import java.lang.reflect.ParameterizedType
import java.util.LinkedList

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline
 *
 * @author Lanscarlos
 * @since 2025/6/12 9:27
 */
@Awake(LifeCycle.LOAD)
object PipelineRegistry : ClassVisitor() {

    data class Registration(val name: String, val event: Class<*>, val pipeline: Class<*>)

    private val registry: HashMap<String, Registration> = linkedMapOf()

    /**
     * 获取相关联的处理流
     * */
    fun getRelatives(name: String): List<Class<*>> {
        val event = mapping(name)
        val registrations = LinkedList<Registration>()
        for (registration in registry.values) {
            if (!registration.event.isAssignableFrom(event)) {
                // 没有继承关系
                continue
            }
            registrations.add(registration)
        }

        // 按继承关系远近进行排序, 继承关系越近则排序越靠前
        registrations.sortedWith { a, b ->
            when {
                a.event == event -> -1 // [a, b]
                b.event == event -> 1 // [b, a]
                a.event.isAssignableFrom(b.event) -> 1 // [b, a]
                else -> -1 // [a, b]
            }
        }

        return registrations.map { it.pipeline }
    }

    /**
     * 解析事件类
     * */
    fun mapping(name: String): Class<*> {
        if (name.isBlank()) {
            error("Invalid name \"$name\"")
        }
        return when {
            name[0] == '@' -> {
                // 虚拟事件类
                return registry[name]?.event ?: error("No event mapping \"$name\" found.")
            }
            name.contains('.') -> {
                // 全类名
                Class.forName(name)
            }
            else -> {
                // 映射 Bukkit Event
                val packages = listOf(
                    "block",
                    "enchantment",
                    "entity",
                    "hanging",
                    "inventory",
                    "player",
                    "raid",
                    "server",
                    "vehicle",
                    "weather",
                    "world"
                )
                for (`package` in packages) {
                    val qualifiedName = "org.bukkit.event.${`package`}.$name"
                    try {
                        return Class.forName(qualifiedName)
                    } catch (_: ClassNotFoundException) {
                        continue
                    }
                }
                error("Cannot auto mapping $name to bukkit event.")
            }
        }
    }

    override fun visitStart(owner: ReflexClass) {
        val clazz = owner.toClass()
        if (clazz.`package`.name != this.javaClass.`package`.name) {
            // 包路径不对
            return
        }
        if (!EventPipeline::class.java.isAssignableFrom(clazz)) {
            // 未继承接口
            return
        }
        if (!clazz.isAnnotationPresent(Pipeline::class.java)) {
            // 没有注解
            return
        }

        // 获取事件类型
        val type = getParameterizedType(clazz)
        if (type == null) {
            warning("Property \"${clazz.name}\" must have a generic type.")
            return
        }

        // 获取名称
        val annotation = clazz.getAnnotation(Pipeline::class.java)
        val name = if (annotation.value.isNotBlank()) {
            // 自定义名称不为空
            "@${annotation.value}"
        } else {
            type.name
        }

        registry[name] = Registration(name, type, clazz)
    }

    /**
     * 获取类的泛型
     * */
    private fun getParameterizedType(clazz: Class<*>): Class<*>? {
        var cache: Class<*> = clazz
        do {
            when (val it = (cache.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)) {
                is Class<*> -> return it
                is ParameterizedType -> return it.rawType as Class<*>
                else -> cache = cache.superclass
            }
        } while (cache.isAnnotationPresent(Rule::class.java))
        return null
    }

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

}