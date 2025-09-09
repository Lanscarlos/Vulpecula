package top.lanscarlos.vulpecula.module.property.entity

import org.bukkit.entity.Damageable
import top.lanscarlos.vulpecula.module.bacikal.property.BacikalProperty
import top.lanscarlos.vulpecula.common.applicative.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since 2025/09/09
 */
object DamageableProperty : BacikalProperty<Damageable> {

    @Suppress("DEPRECATION")
    override fun readProperty(instance: Damageable, key: String): Any {
        return try {
            when(key) {
                "health" -> instance.health
                "absorptionAmount" -> instance.absorptionAmount
                "maxHealth" -> instance.maxHealth
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

    @Suppress("DEPRECATION")
    override fun writeProperty(instance: Damageable, key: String, value: Any?) {
        try {
            when(key) {
                "health" -> instance.health = value.let(DoubleApplicative::convert)
                "absorptionAmount" -> instance.absorptionAmount = value.let(DoubleApplicative::convert)
                "maxHealth" -> instance.maxHealth = value.let(DoubleApplicative::convert)
                else -> throw NoSuchMethodError()
            }
        } catch (ex: NoSuchMethodError) {
            ex.printStackTrace()
            error("Unsupported property: $key")
        }
    }

}