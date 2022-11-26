package top.lanscarlos.vulpecula.kether.action.canvas.pattern

import taboolib.common.util.Location
import taboolib.library.kether.QuestReader
import taboolib.module.kether.ScriptFrame
import top.lanscarlos.vulpecula.internal.ClassInjector
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.canvas.pattern
 *
 * @author Lanscarlos
 * @since 2022-11-10 10:32
 */
interface CanvasPattern {

    /**
     * 获取图案的所有点坐标
     *
     * @param origin 原点
     * @return 坐标集合
     * */
    fun points(origin: Location): Collection<Location>

    /**
     * 获取图案的下一个点坐标
     *
     * @param origin 原点
     * @return 点坐标
     * */
    fun nextPoint(origin: Location): Location

    /**
     * 构建器
     * 用于在脚本中构建 CanvasPattern 对象
     * */
    interface Builder {

        /**
         * 构建方法
         * 用于构建对象
         * */
        fun build(frame: ScriptFrame): CompletableFuture<CanvasPattern>
    }

    /**
     * 用于读取脚本
     * */
    interface Reader {
        /**
         * 图案名
         * 用于注册构建器
         * */
        val name: Array<String>

        /**
         * 用于从脚本中读取属性
         * */
        fun read(reader: QuestReader): Builder

        fun buildNow(func: ScriptFrame.() -> CanvasPattern): Builder {
            return object : Builder {
                override fun build(frame: ScriptFrame): CompletableFuture<CanvasPattern> {
                    return CompletableFuture.completedFuture(func(frame))
                }
            }
        }

        fun buildFuture(func: ScriptFrame.() -> CompletableFuture<CanvasPattern>): Builder {
            return object : Builder {
                override fun build(frame: ScriptFrame): CompletableFuture<CanvasPattern> {
                    return func(frame)
                }
            }
        }
    }

    companion object : ClassInjector(packageName = CanvasPattern::class.java.packageName) {

        private val registry = HashMap<String, Reader>()

        fun getReader(name: String): Reader? {
            return registry[name.lowercase()]
        }

        fun register(reader: Reader) {
            reader.name.forEach {
                registry[it.lowercase()] =  reader
            }
        }

        override fun visitStart(clazz: Class<*>, supplier: Supplier<*>?) {
            if (!Reader::class.java.isAssignableFrom(clazz)) return

            val reader = let {
                if (supplier?.get() != null) {
                    supplier.get()
                } else try {
                    clazz.getDeclaredConstructor().newInstance()
                } catch (e: Exception) {
                    null
                }
            } as? Reader ?: return

            register(reader)
        }
    }
}