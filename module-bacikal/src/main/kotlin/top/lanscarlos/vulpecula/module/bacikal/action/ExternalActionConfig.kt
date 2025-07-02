package top.lanscarlos.vulpecula.module.bacikal.action

import top.lanscarlos.vulpecula.common.config.LiveData
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.action
 *
 * @author Lanscarlos
 * @since 2025/7/2 10:59
 */

fun Any.bindActionConfig(vararg path: String): LiveData<Any?> {
    val name = this.javaClass.name
    val source = BacikalRegistry.sources[name] ?: error("Source $name not found")
    return source.config.read(*path)
}