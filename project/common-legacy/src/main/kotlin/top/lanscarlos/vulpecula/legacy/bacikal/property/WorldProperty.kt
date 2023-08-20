package top.lanscarlos.vulpecula.legacy.bacikal.property

import org.bukkit.World
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalProperty
import top.lanscarlos.vulpecula.legacy.bacikal.BacikalGenericProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.property
 *
 * @author Lanscarlos
 * @since 2023-03-22 14:03
 */
@BacikalProperty(
    id = "world",
    bind = World::class
)
class WorldProperty : BacikalGenericProperty<World>("world") {
    override fun readProperty(instance: World, key: String): OpenResult {
        val property: Any? = when (key) {
            "name" -> instance.name
            else -> return OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun writeProperty(instance: World, key: String, value: Any?): OpenResult {
        return OpenResult.failed()
    }
}