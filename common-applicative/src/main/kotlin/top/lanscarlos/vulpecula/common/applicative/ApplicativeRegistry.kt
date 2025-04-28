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
 * 注册表, 自动检索并注册 Applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 12:42
 */
@Awake(LifeCycle.LOAD)
object ApplicativeRegistry : ClassVisitor(-4) {

    override fun getLifeCycle(): LifeCycle = LifeCycle.LOAD

    private val registry = mutableMapOf<Class<*>, Applicative<*>>() // 注册的 Applicative

    private val nameMapping = mutableMapOf<String, Applicative<*>>() // 名称映射

    /**
     * 获取对应的 Applicative
     * */
    fun <T> getApplicative(name: String): Applicative<T> {
        require(nameMapping.containsKey(name)) { "Applicative for \"$name\" not found." }
        return getApplicativeOrNull(name)!!
    }

    /**
     * 获取对应的 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T> getApplicativeOrNull(name: String): Applicative<T>? {
        return nameMapping[name] as? Applicative<T>
    }

    /**
     * 获取对应的 Applicative
     * */
    fun <T> getApplicative(clazz: Class<T>): Applicative<T> {
        require(registry.containsKey(clazz)) { "Applicative for \"${clazz.name}\" not found." }
        return getApplicativeOrNull(clazz)!!
    }

    /**
     * 获取对应的 Applicative
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T> getApplicativeOrNull(clazz: Class<T>): Applicative<T>? {
        return registry[clazz] as? Applicative<T>
    }

    /**
     * 注册 Applicative
     * */
    fun registerApplicative(clazz: Class<*>, applicative: Applicative<*>) {
        if (registry.containsKey(clazz)) {
            warning("Applicative for \"${clazz.name}\" already exists. It will be replaced.")
        }
        nameMapping[applicative.name] = applicative
        for (alias in applicative.aliases) {
            nameMapping[alias] = applicative
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