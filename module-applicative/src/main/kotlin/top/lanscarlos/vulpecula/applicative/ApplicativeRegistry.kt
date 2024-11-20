package top.lanscarlos.vulpecula.applicative

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ReflexClass
import java.lang.reflect.ParameterizedType
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 12:42
 */
@Awake(LifeCycle.LOAD)
object ApplicativeRegistry : ClassVisitor(-4) {

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

    val registry = mutableMapOf<Class<*>, Applicative<*>>()

    /**
     * 获取对应的 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T> getApplicative(clazz: Class<T>): Applicative<T>? {
        return registry[clazz] as? Applicative<T>
    }

    /**
     * 获取相关联的 Applicative, 按照继承关系远近排序, 优先选择最近的父类
     *
     * @return 相关联的所有 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T> getRelatedApplicative(clazz: Class<T>): List<Applicative<in T>> {
        return registry.filterKeys {
            it.isAssignableFrom(clazz)
        }.map {
            it.key to it.value
        }.sortedWith { a, b ->
            when {
                a.first == clazz -> -1
                b.first == clazz -> 1
                a.first.isAssignableFrom(b.first) -> 1
                else -> -1
            }
        }.map {
            it.second as Applicative<in T>
        }
    }

    /**
     * 注册 Applicative
     * */
    fun registerApplicative(clazz: Class<*>, applicative: Applicative<*>) {
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
        val type = (clazz.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0) as? Class<*> ?: let {
            warning("Property \"${clazz.name}\" must have a generic type.")
            return
        }

        // 注册
        registerApplicative(type, applicative)
    }

}