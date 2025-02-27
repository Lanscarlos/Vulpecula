package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ReflexClass
import java.lang.reflect.ParameterizedType

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 12:42
 */
@Awake(LifeCycle.LOAD)
object ApplicativeRegistry : ClassVisitor(-4) {

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

    val related = linkedMapOf<Class<*>, RelatedApplicative<*>>() // 缓存相关联的 Applicative

    val registry = mutableMapOf<Class<*>, Applicative<*>>() // 注册的 Applicative

    /**
     * 获取对应的 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getApplicative(clazz: Class<T>): Applicative<T> {
        return registry[clazz] as? Applicative<T> ?: error("Applicative for \"${clazz.name}\" not found.")
    }

    /**
     * 获取相关联的 Applicative, 按照继承关系远近排序, 优先选择最近的父类
     *
     * @return 相关联的所有 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getRelatedApplicative(clazz: Class<T>): Applicative<T> {
        return related.computeIfAbsent(clazz) { key ->
            val relatedApplicatives = registry.filterKeys {
                it.isAssignableFrom(key)
            }.map {
                it.key to it.value
            }.sortedWith { a, b ->
                when {
                    a.first == key -> -1
                    b.first == key -> 1
                    a.first.isAssignableFrom(b.first) -> 1
                    else -> -1
                }
            }.map {
                it.second as Applicative<in T>
            }
            RelatedApplicative(getApplicative(clazz)!!, relatedApplicatives)
        } as Applicative<T>
    }

    /**
     * 注册 Applicative
     * */
    fun registerApplicative(clazz: Class<*>, applicative: Applicative<*>) {
        if (registry.containsKey(clazz)) {
            warning("Applicative for \"${clazz.name}\" already exists. It will be replaced.")
        }
        registry[clazz] = applicative
    }

    override fun visitStart(owner: ReflexClass) {
        val clazz = owner.toClass()
        if (clazz.name.contains("taboolib")) {
            // 排除 taboolib 库
            return
        }
        if (clazz == AbstractApplicative::class.java || !AbstractApplicative::class.java.isAssignableFrom(clazz)) {
            // 必须继承 AbstractApplicative
            return
        }

        // 获取实例
        val applicative = try {
            // 尝试实例化
            (owner.getInstance() ?: clazz.getDeclaredConstructor().newInstance()) as Applicative<*>
        } catch (ex: Exception) {
            warning("Property \"${clazz.name}\" must have a empty constructor.")
            return
        }

        // 获取泛型类型
        val type = when (val it = (clazz.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0)) {
            is Class<*> -> it
            is ParameterizedType -> it.rawType as Class<*>
            else -> {
                warning("Property \"${clazz.name}\" must have a generic type.")
                return
            }
        }

        // 注册
        registerApplicative(type, applicative)
    }

}