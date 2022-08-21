package top.lanscarlos.vulpecula

import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.internal.EventDispatcher
import top.lanscarlos.vulpecula.internal.EventHandler
import top.lanscarlos.vulpecula.internal.EventListener
import top.lanscarlos.vulpecula.internal.EventMapping
import top.lanscarlos.vulpecula.utils.Debug

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2022-08-19 17:44
 */
object VulpeculaContext {

    fun load(config: Configuration) {
        Debug.load(config)
        EventMapping.load()
        EventDispatcher.preLoad()
        EventHandler.load()
        EventDispatcher.postLoad()
        EventListener.registerAll()
    }

}

typealias Context = VulpeculaContext