package top.lanscarlos.vulpecula.kether.property

import org.bukkit.entity.Entity
import taboolib.common.OpenResult
import taboolib.common.platform.function.info
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * Entity 泛型属性
 * 兼容 Entity 下的所有类
 *
 * @author Lanscarlos
 * @since 2022-10-19 11:40
 */

@VulKetherProperty(
    id = "entity-generic",
    bind = Entity::class,
    generic = true,
)
class EntityGenericProperty : GenericProperty<Entity>("entity-generic")

abstract class GenericProperty<T : Any>(
    id: String
) : VulScriptProperty<T>(id) {

    override fun read(instance: T, key: String): OpenResult {
        KetherRegistry.getScriptProperties(instance).forEach {
            val result = it.read(instance, key)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }

    override fun write(instance: T, key: String, value: Any?): OpenResult {
        KetherRegistry.getScriptProperties(instance).forEach {
            val result = it.write(instance, key, value)
            if (result.isSuccessful) return result
        }
        return OpenResult.failed()
    }
}