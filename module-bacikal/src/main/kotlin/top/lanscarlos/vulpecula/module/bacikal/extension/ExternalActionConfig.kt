package top.lanscarlos.vulpecula.module.bacikal.extension

import top.lanscarlos.vulpecula.common.config.LiveData
import top.lanscarlos.vulpecula.common.config.read
import top.lanscarlos.vulpecula.module.bacikal.BacikalRegistry

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.extension
 *
 * @author Lanscarlos
 * @since 2025/10/11
 */

fun Any.bindActionConfig(vararg path: String): LiveData<Any?> {
    val source = BacikalRegistry.getExtensionByClass(this.javaClass)
    return source.config.read(*path)
}