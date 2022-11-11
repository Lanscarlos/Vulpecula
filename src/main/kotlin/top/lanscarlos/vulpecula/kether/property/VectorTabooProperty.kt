package top.lanscarlos.vulpecula.kether.property

import taboolib.common.OpenResult
import taboolib.common.util.Vector
import top.lanscarlos.vulpecula.kether.VulKetherProperty
import top.lanscarlos.vulpecula.kether.VulScriptProperty
import top.lanscarlos.vulpecula.utils.toDouble

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.property
 *
 * @author Lanscarlos
 * @since 2022-11-11 23:49
 */
@VulKetherProperty(
    id = "vector-taboolib",
    bind = Vector::class,
)
class VectorTabooProperty : VulScriptProperty<Vector>("vector-taboolib") {

    override fun read(instance: Vector, key: String): OpenResult {
        val property: Any = when (key) {
            "clone" -> instance.clone()
            "bukkit" -> org.bukkit.util.Vector(instance.x, instance.y, instance.z)
            "blockX", "block-x" -> instance.blockX
            "blockY", "block-y" -> instance.blockY
            "blockZ", "block-z" -> instance.blockZ
            "x" -> instance.x
            "y" -> instance.y
            "z" -> instance.z
            "length" -> instance.length()
            "lengthSquared", "length-squared", "length-sq", "squared", "sq" -> instance.lengthSquared()
            "isNormalized", "normalized" -> instance.isNormalized
            "normalize" -> instance.clone().normalize()
            "zero" -> instance.clone().zero()
            else -> OpenResult.failed()
        }
        return OpenResult.successful(property)
    }

    override fun write(instance: Vector, key: String, value: Any?): OpenResult {
        when (key) {
            "x" -> {
                instance.x = value?.toDouble() ?: return OpenResult.successful()
            }
            "y" -> {
                instance.y = value?.toDouble() ?: return OpenResult.successful()
            }
            "z" -> {
                instance.z = value?.toDouble() ?: return OpenResult.successful()
            }
            else -> return OpenResult.failed()
        }
        return OpenResult.successful()
    }
}