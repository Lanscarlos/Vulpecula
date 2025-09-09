package top.lanscarlos.vulpecula.common.applicative

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import taboolib.library.reflex.ReflexClass

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

    internal val registry = mutableMapOf<Class<*>, Applicative<*>>() // 注册的 Applicative

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
        val cache = registry[clazz] as? Applicative<T>
        if (cache != null) {
            return cache
        }
        if (clazz.enumConstants != null) {
            // 枚举类
            val applicative = EnumApplicative(clazz)
            registry[clazz] = applicative
            return applicative
        }
        return null
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
        // 处理基本类型映射
        when (clazz) {
            Boolean::class.java -> registry[java.lang.Boolean::class.java] = applicative
            Byte::class.java -> registry[java.lang.Byte::class.java] = applicative
            Short::class.java -> registry[java.lang.Short::class.java] = applicative
            Int::class.java -> registry[Integer::class.java] = applicative
            Long::class.java -> registry[java.lang.Long::class.java] = applicative
            Float::class.java -> registry[java.lang.Float::class.java] = applicative
            Double::class.java -> registry[java.lang.Double::class.java] = applicative
        }
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
            (owner.getInstance() ?: clazz.getDeclaredConstructor().newInstance()) as AbstractApplicative<*>
        } catch (_: Exception) {
            warning("Property \"${clazz.name}\" must have a empty constructor.")
            return
        }

        // 注册
        registerApplicative(applicative.clazz, applicative)
    }

}