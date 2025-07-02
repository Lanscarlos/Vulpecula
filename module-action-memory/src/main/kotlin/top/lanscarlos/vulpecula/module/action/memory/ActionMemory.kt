package top.lanscarlos.vulpecula.module.action.memory

import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.core.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.action.bindActionConfig
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

object ActionMemory {

    val defaultOwner: String by bindActionConfig("default-owner").string("@")

    val defaultStorage: String by bindActionConfig("default-storage").string("vulpecula")

}

@BacikalParser("memory.get")
object ActionMemoryGet : ClassActionResolver {

    fun resolve(
        key: String,
        @Additional(["owner"]) owner: String = ActionMemory.defaultOwner,
        @Additional(["storage"]) storage: String = ActionMemory.defaultStorage
    ): Any? {
        return getStorage(storage).get(key, owner)
    }

}

@BacikalParser("memory.set")
object ActionMemorySet : ClassActionResolver {

    fun resolve(
        key: String,
        value: Any?,
        @Additional(["owner"]) owner: String = ActionMemory.defaultOwner,
        @Additional(["storage"]) storage: String = ActionMemory.defaultStorage
    ): Any? {
        if (value == null) {
            return getStorage(storage).remove(key, owner)
        }
        return getStorage(storage).set(key, value, owner)
    }

}

@BacikalParser("memory.remove")
object ActionMemoryRemove : ClassActionResolver {

    fun resolve(
        key: String,
        @Additional(["owner"]) owner: String = ActionMemory.defaultOwner,
        @Additional(["storage"]) storage: String = ActionMemory.defaultStorage
    ): Any? {
        return getStorage(storage).remove(key, owner)
    }

}

private fun getStorage(storage: String): MemoryStorage {
    return when (storage.lowercase()) {
        "vulpecula" -> VulpeculaStorage
        else -> error(asLang("module-action-memory-exception-invalid-storage", storage))
    }
}