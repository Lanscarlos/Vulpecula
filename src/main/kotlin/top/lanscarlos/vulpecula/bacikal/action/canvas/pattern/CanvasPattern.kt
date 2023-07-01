package top.lanscarlos.vulpecula.bacikal.action.canvas.pattern

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.util.Location
import top.lanscarlos.vulpecula.bacikal.Bacikal
import top.lanscarlos.vulpecula.bacikal.BacikalReader
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.internal.ClassInjector
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 10:32
 */
interface CanvasPattern {

    /**
     * 获取图案的下一个点坐标
     *
     * @param origin 原点，当内置原点时，优先使用内置原点
     * @return 点坐标
     * */
    fun point(origin: Location): Location

    /**
     * 获取图案的所有点坐标
     *
     * @param origin 原点，当内置原点时，优先使用内置原点
     * @return 坐标集合
     * */
    fun shape(origin: Location): Collection<Location>

    interface Resolver {

        /**
         * 图案名
         * 用于注册构建器
         * */
        val name: Array<String>

        fun resolve(
            reader: BacikalReader,
            nameLiveData: LiveData<String>,
            originLiveData: LiveData<Location?>
        ): Bacikal.Parser<CanvasPattern>
    }

    @Awake(LifeCycle.LOAD)
    companion object : ClassInjector() {

        private val registry = HashMap<String, Resolver>()

        fun getResolver(name: String): Resolver? {
            return registry[name.lowercase()]
        }

        fun register(reader: Resolver) {
            reader.name.forEach {
                registry[it.lowercase()] = reader
            }
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

            register(resolver)
        }
    }
}