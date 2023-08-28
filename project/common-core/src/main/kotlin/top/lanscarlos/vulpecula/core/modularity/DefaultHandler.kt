package top.lanscarlos.vulpecula.core.modularity

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest
import top.lanscarlos.vulpecula.config.DynamicConfig
import top.lanscarlos.vulpecula.modularity.ModularHandler
import top.lanscarlos.vulpecula.modularity.Module
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.modularity
 *
 * @author Lanscarlos
 * @since 2023-09-01 21:07
 */
class DefaultHandler(
    override val module: Module,
    override val id: String,
    val config: DynamicConfig
) : ModularHandler {

    override val file: File
        get() = config.file

    override val bind: List<String> by config.readStringList("$id.bind", emptyList())

    override fun buildQuest(): BacikalQuest {
        TODO("Not yet implemented")
    }

}