package top.lanscarlos.vulpecula.module.action.memory

import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */
@BacikalParser("memory.get")
object ActionMemoryGet : ClassActionResolver {

    fun resolve(
        key: String,
        @Additional(["namespace"]) namespace: String = ActionMemory.DEFAULT_NAMESPACE,
        @Additional(["storage"]) storage: String = ActionMemory.DEFAULT_STORAGE,
    ): Any? {
        val storage = ActionMemory.getStorage(storage)
        return storage.get(key, namespace)
    }

}