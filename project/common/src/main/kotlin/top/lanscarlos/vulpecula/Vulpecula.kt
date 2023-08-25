package top.lanscarlos.vulpecula

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

/**
 * Vulpecula
 * top.lanscarlos.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-08-14 17:07
 */
object Vulpecula {

    @Config
    lateinit var config: Configuration
        private set
}