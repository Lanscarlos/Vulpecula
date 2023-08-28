package top.lanscarlos.vulpecula.modularity

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

    val handlers: Map<String, ModularHandler>

    /**
     * 启用模块
     * */
    fun enable()

    /**
     * 禁用模块
     * */
    fun disable()

}