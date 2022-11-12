package top.lanscarlos.vulpecula.kether.property

import org.bukkit.entity.Entity
import org.bukkit.event.Event
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-10-19 11:40
 */

/**
 * Entity 泛型属性
 * 兼容 Entity 下的所有类
 * */
@VulKetherProperty(
    id = "entity-generic",
    bind = Entity::class,
    generic = true
)
class EntityGenericProperty : GenericProperty<Entity>("entity-generic")

/**
 * Event 泛型属性
 * */
@VulKetherProperty(
    id = "event-generic",
    bind = Event::class,
    generic = true
)
class EventGenericProperty : GenericProperty<Event>("event-generic")

/**
 * 泛型属性
 *
 * 支持兼容查找模式以及遍历节点功能
 * */
abstract class GenericProperty<T : Any>(
    id: String
) : VulScriptProperty<T>(id) {

    override fun read(instance: T, key: String): OpenResult {
        return if (key.contains('.')) {
            val path = key.split('.')
            readPropertyDeep(instance, path)
        } else {
            readProperty(instance, key)
        }
    }

    override fun write(instance: T, key: String, value: Any?): OpenResult {
        return if (key.contains('.')) {
            val path = key.split('.')
            writePropertyDeep(instance, path, value)
        } else {
            writeProperty(instance, key, value)
        }
    }

    private fun readPropertyDeep(instance: T, path: List<String>): OpenResult {
        if (path.isEmpty()) {
            return OpenResult.failed()
        } else if (path.size < 2) {
            return readProperty(instance, path.first())
        }

        var cache: Any = instance
        for (i in 0 until path.lastIndex) {
            // 遍历除最后一个外所有节点
            cache = readProperty(cache, path[i]).let {
                if (it.isSuccessful) it.value else null
            } ?: return OpenResult.failed()
        }
        return readProperty(cache, path.last())
    }

    private fun writePropertyDeep(instance: T, path: List<String>, value: Any?): OpenResult {
        if (path.isEmpty()) {
            return OpenResult.failed()
        } else if (path.size < 2) {
            return writeProperty(instance, path.first(), value)
        }

        var cache: Any = instance
        for (i in 0 until path.lastIndex) {
            // 遍历除最后一个外所有节点
            cache = readProperty(cache, path[i]).let {
                if (it.isSuccessful) it.value else null
            } ?: return OpenResult.failed()
        }
        return writeProperty(cache, path.last(), value)
    }

    private fun <R> readProperty(instance: R, key: String): OpenResult {
        KetherRegistry.getScriptProperties(instance).forEach {
            val result = it.read(instance, key)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }

    private fun <R> writeProperty(instance: R, key: String, value: Any?): OpenResult {
        KetherRegistry.getScriptProperties(instance).forEach {
            val result = it.write(instance, key, value)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }
}