package top.lanscarlos.vulpecula.kether.property

import org.bukkit.World
import taboolib.common.OpenResult
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2023-01-22 23:49
 */
@VulKetherProperty(
    id = "world",
    bind = World::class
)
class WorldProperty : VulScriptProperty<World>("world") {
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