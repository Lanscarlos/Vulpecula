package top.lanscarlos.vulpecula.script

import taboolib.module.configuration.Configuration
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.common.livedata.string
import top.lanscarlos.vulpecula.common.livedata.stringListOrNull
import top.lanscarlos.vulpecula.common.livedata.stringOrNull

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.script
 *
 * @author Lanscarlos
 * @since 2025-03-20 15:11
 */
class YamlScript(val config: Configuration) : Script {

    val namespace by config.read("namespace").stringListOrNull()

    val mainSource by config.read("main").stringOrNull()

}