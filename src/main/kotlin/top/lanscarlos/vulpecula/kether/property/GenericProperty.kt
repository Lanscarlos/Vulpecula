package top.lanscarlos.vulpecula.kether.property

import org.bukkit.entity.Entity
import org.bukkit.event.Event
import taboolib.common.OpenResult
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
class EntityGenericProperty : GenericProperty<Entity>("entity")

/**
 * Event 泛型属性
 * */
@VulKetherProperty(
    id = "event-generic",
    bind = Event::class,
    generic = true
)
class EventGenericProperty : GenericProperty<Event>("event")

/**
 * 泛型属性
 *
 * 支持兼容查找模式以及遍历节点功能
 * */
abstract class GenericProperty<T : Any>(
    id: String
) : VulScriptProperty<T>("$id-generic") {

    override fun readProperty(instance: T, key: String): OpenResult {
        return readGenericProperty(instance, key)
    }

    override fun writeProperty(instance: T, key: String, value: Any?): OpenResult {
        return writeGenericProperty(instance, key, value)
    }
}