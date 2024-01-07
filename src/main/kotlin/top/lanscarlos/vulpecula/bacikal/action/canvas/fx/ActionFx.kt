package top.lanscarlos.vulpecula.bacikal.action.canvas.fx

import top.lanscarlos.vulpecula.bacikal.*
import top.lanscarlos.vulpecula.internal.ClassInjector
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.fx
 *
 * @author Lanscarlos
 * @since 2023-06-29 13:19
 */
object ActionFx : ClassInjector() {

    private val registry = mutableMapOf<String, Resolver>()

    /**
     * 向 Fx 语句注册子语句
     * @param resolver 子语句解析器
     * */
    fun registerResolver(resolver: Resolver) {
        resolver.name.forEach { registry[it.lowercase()] = resolver }
    }

    override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
        if (!Resolver::class.java.isAssignableFrom(clazz)) return

        val resolver = let {
            if (supplier?.get() != null) {
                supplier.get()
            } else try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                null
            }
        } as? Resolver ?: return

        registerResolver(resolver)
    }

    @BacikalParser("canvas-fx")
    fun parser() = bacikal {
        combine(
            LiveData {
                val next = nextToken()
                val parser = registry[next.lowercase()]?.resolve(this) ?: error("Unknown sub action \"$next\" at fx action.")
                Bacikal.Action {
                    parser.action.run(it)
                }
            }
        ) { fx ->
            fx
        }
    }

    /**
     * 语句解析器
     * */
    interface Resolver {

        val name: Array<String>

        fun resolve(reader: BacikalReader): Bacikal.Parser<Fx<*, *>>
    }
}