package top.lanscarlos.vulpecula.modularity

import top.lanscarlos.vulpecula.bacikal.BacikalWorkspace
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-08-29 16:46
 */
interface Module {

    val id: String

    val directory: File

    val dispatchers: Map<String, ModularDispatcher>

    val workspace: BacikalWorkspace

    /**
     * 启用模块
     * */
    fun enable()

    /**
     * 禁用模块
     * */
    fun disable()

}