package top.lanscarlos.vulpecula.module.action.memory

import top.lanscarlos.vulpecula.common.config.string
import top.lanscarlos.vulpecula.common.utils.asLang
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.Parser
import top.lanscarlos.vulpecula.module.bacikal.extension.bindActionConfig
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalFrame
import top.lanscarlos.vulpecula.module.bacikal.parser.ClassActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.action.memory
 *
 * @author Lanscarlos
 * @since 2025/6/30
 */

object ActionMemory {

    internal const val CONTEXT = "@VULPECULA_CONTEXT_MEMORY"

    internal val defaultOwner: String by bindActionConfig("default-owner").string("@")

    internal val defaultStorage: String by bindActionConfig("default-storage").string("vulpecula")

}

@Parser("memory.switch")
object ActionMemorySwitch : ClassActionResolver {

    fun resolve(frame: BacikalFrame, storage: String) {
        frame.setVariable(ActionMemory.CONTEXT, getStorageByName(storage))
    }

}

@Parser("memory.get")
object ActionMemoryGet : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        key: String,
        @Additional(["owner"]) owner: String = ActionMemory.defaultOwner,
        @Additional(["storage"]) storage: String = ActionMemory.defaultStorage
    ): Any? {
        return getStorage(frame, storage).get(key, owner)
    }

}

@Parser("memory.set")
object ActionMemorySet : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        key: String,
        value: Any?,
        @Additional(["owner"]) owner: String = ActionMemory.defaultOwner,
        @Additional(["storage"]) storage: String = ActionMemory.defaultStorage
    ): Any? {
        if (value == null) {
            return getStorage(frame, storage).remove(key, owner)
        }
        return getStorage(frame, storage).set(key, value, owner)
    }

}

@Parser("memory.remove")
object ActionMemoryRemove : ClassActionResolver {

    fun resolve(
        frame: BacikalFrame,
        key: String,
        @Additional(["owner"]) owner: String = ActionMemory.defaultOwner,
        @Additional(["storage"]) storage: String = ActionMemory.defaultStorage
    ): Any? {
        return getStorage(frame, storage).remove(key, owner)
    }

}

private fun getStorage(frame: BacikalFrame, storage: String): MemoryStorage {
    return frame.getVariable<MemoryStorage>(ActionMemory.CONTEXT)
        ?: getStorageByName(storage)
}

private fun getStorageByName(name: String): MemoryStorage {
    return when (name.lowercase()) {
        "vulpecula" -> VulpeculaStorage
        else -> error(asLang("module-action-memory-exception-invalid-storage", name))
    }
}