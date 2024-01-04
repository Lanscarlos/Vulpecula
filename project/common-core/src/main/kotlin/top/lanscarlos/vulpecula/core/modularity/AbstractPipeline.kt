package top.lanscarlos.vulpecula.core.modularity

import org.bukkit.event.Event
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.function.warning
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.modularity.DispatcherPipeline
import java.lang.reflect.ParameterizedType
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity
 *
 * @author Lanscarlos
 * @since 2023-12-28 01:10
 */
abstract class AbstractPipeline<T : Event>(val config: DynamicConfig) : DispatcherPipeline<T> {

    @Awake(LifeCycle.LOAD)
    companion object : ClassVisitor() {

        const val VARIABLE_PLAYER = "@Player"

        private val registry = mutableMapOf<Class<out Event>, Class<DispatcherPipeline<in Event>>>()

        /**
         * 实例化事件处理流水线
         * */
        fun <T : Event> newInstance(event: Class<T>, config: DynamicConfig): DispatcherPipeline<in Event>? {
            return try {
                registry[event]?.getDeclaredConstructor(DynamicConfig::class.java)?.newInstance(config)
            } catch (ex: Exception) {
                warning("DispatcherPipeline of $event create failed：${ex.localizedMessage}")
                null
            }
        }

        /**
         * 生成事件处理流水线
         * 处理顺序为父类到子类
         * */
        fun <T : Event> generate(event: Class<T>, config: DynamicConfig): List<DispatcherPipeline<in Event>> {
            return registry.keys.filter {
                it.isAssignableFrom(event)
            }.sortedWith { c1, c2 ->
                if (c1.isAssignableFrom(c2)) -1 else 1
            }.mapNotNull {
                newInstance(it, config)
            }
        }

        /**
         * 注册事件处理流水线
         * */
        fun <T : Event> register(event: Class<T>, pipeline: Class<DispatcherPipeline<in Event>>) {
            registry[event] = pipeline
        }

        @Suppress("UNCHECKED_CAST")
        override fun visitStart(clazz: Class<*>, instance: Supplier<*>?) {
            if (clazz == AbstractPipeline::class.java || !AbstractPipeline::class.java.isAssignableFrom(clazz)) {
                return
            }
            try {
//                val event = clazz.kotlin.supertypes.firstOrNull()?.arguments?.firstOrNull()?.type?.classifier as? KClass<out Event> ?: return
                val type = (clazz.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.getOrNull(0) ?: return
                val event = (type as? Class<out Event>) ?: return
                register(event, clazz as Class<DispatcherPipeline<in Event>>)
            } catch (ignored: Exception) {
            }
        }

        override fun getLifeCycle() = LifeCycle.LOAD
    }
}