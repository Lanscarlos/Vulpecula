package top.lanscarlos.vulpecula.module.dispatcher.rule

import taboolib.library.configuration.ConfigurationSection
import taboolib.library.reflex.ReflexClass

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.rule
 *
 * @author Lanscarlos
 * @since 2025/6/11 13:21
 */
@Rule("PlayerShootBowEvent")
class PlayerShootBowEventRule(clazz: ReflexClass, config: ConfigurationSection) : EntityShootBowEventRule(clazz, config) {

    override val playerRequired: Boolean = true

}