package top.lanscarlos.vulpecula.module.dispatcher.pipeline

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.library.reflex.ReflexClass
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.dispatcher.Pipeline
import top.lanscarlos.vulpecula.module.dispatcher.warning
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

    data class Registration(val name: String, val extends: String, val event: Class<*>, val pipeline: Class<*>) {

        val isVirtual: Boolean = name[0] == '@'

        var parent: Registration? = null

    }

    private val registry: HashMap<String, Registration> = linkedMapOf()

    @Awake(LifeCycle.INIT)
    fun onInit() {
        registerLifeCycleTask(LifeCycle.LOAD, 4, runnable = ::onLoad)
    }

    fun onLoad() {
        for (registration in registry.values) {
            if (registration.name[0] != '@') {
                continue
            }
            val extends: String = registration.extends.takeIf(String::isNotBlank) ?: continue
            val parent: Registration = registry[extends] ?: error("Parent $extends in ${registration.name} is not registered.")
            registration.parent = parent
        }
    }

    /**
     * 获取相关联的处理流
     * */
    fun getRelatives(name: String): List<Class<*>> {
        val event = mapping(name)
        val registrations = LinkedList<Registration>()
        for (registration in registry.values.filter { it.isVirtual.not() }) {
            if (!registration.event.isAssignableFrom(event)) {
                // 没有继承关系
                continue
            }
            registrations.add(registration)
        }

        if (name[0] == '@') {
            // 获取继承链条上所有的虚拟事件
            var registration: Registration? = registry.values.first { it.name == name }
            while (registration != null) {
                registrations.add(registration)
                registration = registration.parent ?: break
            }
        }

        // 按继承关系远近进行排序, 继承关系越远则排序越靠前, 越先处理事件
        registrations.sortedWith { a, b ->
            when {
                a.event == b.event -> {
                    // 两者相同, 可能为虚拟事件, 比对 name 和 extends 字段
                    when {
                        a.name[0] == '@' && b.name[0] != '@' -> {
                            // a 为虚拟事件, 继承自 b
                            1 // [b, a]
                        }
                        a.name[0] != '@' && b.name[0] == '@' -> {
                            // b 为虚拟事件, 继承自 a
                            -1 // [a, b]
                        }
                        a.extends.isBlank() && b.extends.isBlank() -> 0
                        a.extends == b.name ->  1 // [b, a]
                        b.extends == a.name ->  -1 // [a, b]
                        else -> 0
                    }
                }
                a.event == event -> 1 // [b, a]
                b.event == event -> -1 // [a, b]
                a.event.isAssignableFrom(b.event) -> -1 // [a, b]
                else -> 0
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
        if (!clazz.`package`.name.startsWith(this.javaClass.`package`.name)) {
            // 包路径不对
            return
        }
        if (!Pipeline::class.java.isAssignableFrom(clazz)) {
            // 未继承接口
            return
        }
        if (!clazz.isAnnotationPresent(AutoRegistered::class.java)) {
            // 没有注解
            return
        }

        // 获取事件类型
        val type = try {
            getParameterizedType(clazz)
        } catch (ex: TypeNotPresentException) {
            // 事件类不存在
            console().warning { asLang("module-dispatcher-exception-event-class-not-found", ex.typeName()) }
            return
        } catch (_: NullPointerException) {
            console().warning { "Property \"${clazz.name}\" must have a generic type." }
            return
        }

        // 获取名称
        val annotation = clazz.getAnnotation(AutoRegistered::class.java)
        val name = if (annotation.value.isNotBlank()) {
            // 自定义名称不为空
            "@${annotation.value}"
        } else {
            type.name
        }

        registry[name] = Registration(name, annotation.extends, type, clazz)
    }

    /**
     * 获取类的泛型
     * */
    private fun getParameterizedType(clazz: Class<*>): Class<*> {
        var cache: Class<*> = clazz
        do {
            when (val it = (cache.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)) {
                is Class<*> -> return it
                is ParameterizedType -> return it.rawType as Class<*>
                else -> cache = cache.superclass
            }
        } while (cache.isAnnotationPresent(AutoRegistered::class.java))
        throw NullPointerException()
    }

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

}