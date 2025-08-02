package top.lanscarlos.vulpecula.module.dispatcher.pipeline.player

import org.bukkit.event.entity.EntityDamageByEntityEvent
import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.module.dispatcher.pipeline.AutoRegistered

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.dispatcher.pipeline.player
 *
 * @author Lanscarlos
 * @since 2025/7/29
 */
@AutoRegistered("PlayerDamageByEntityEvent", "PlayerDamageEvent")
class PlayerDamageByEntityEventPipeline(clazz: Class<*>, config: ConfigurationSection) : VirtualPlayerEventPipeline<EntityDamageByEntityEvent>(clazz, config)