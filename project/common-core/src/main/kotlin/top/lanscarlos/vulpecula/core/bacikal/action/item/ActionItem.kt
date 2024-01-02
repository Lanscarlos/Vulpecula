package top.lanscarlos.vulpecula.core.bacikal.action.item

import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import top.lanscarlos.vulpecula.bacikal.bacikalParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalContext
import top.lanscarlos.vulpecula.bacikal.parser.BacikalFruit
import top.lanscarlos.vulpecula.bacikal.parser.BacikalParser
import java.util.function.Function
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.bacikal.action.item
 *
 * @author Lanscarlos
 * @since 2023-08-29 10:56
 */
@Awake(LifeCycle.LOAD)
object ActionItem : ClassVisitor(-2) {

    private val registry = mutableMapOf<String, Resolver>()

    interface Resolver {

        val name: Array<String>

        val resolver: BacikalContext.() -> BacikalFruit<*>

    }

    @BacikalParser("item")
    fun parser() = bacikalParser {
        val next = reader.readToken()
        registry[next.lowercase()]?.resolver?.invoke(this)
            ?: error("Unknown sub action \"$next\" at item action.")
    }

    /**
     * 注册子语句解析器
     * */
    fun registerResolver(name: Array<String>, func: Function<BacikalContext, BacikalFruit<*>>) {
        registerResolver(
            object : Resolver {
                override val name = name
                override val resolver = func::apply
            }
        )
    }

    /**
     * 注册子语句解析器
     * */
    fun registerResolver(resolver: Resolver) {
        resolver.name.forEach { registry[it.lowercase()] = resolver }
    }

    override fun getLifeCycle() = LifeCycle.LOAD

    override fun visitStart(clazz: Class<*>, instance: Supplier<*>?) {
        if (!Resolver::class.java.isAssignableFrom(clazz)) {
            return
        }

        val resolver = let {
            if (instance?.get() != null) {
                instance.get()
            } else try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                null
            }
        } as? Resolver ?: return

        registerResolver(resolver)
    }

}