package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection
import top.lanscarlos.vulpecula.common.livedata.LiveData

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 16:53
 */

fun ConfigurationSection.read(vararg keys: String): LiveData<Any?> {
    return DelegateConfigNode(this, keys)
}