package top.lanscarlos.vulpecula.core.modularity

import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-08-30 23:01
 */
interface ModularComponent {

    /**
     * 组件 ID
     * */
    val id: String

    /**
     * 所在文件
     * */
    val file: File

    /**
     * 所属模块
     * */
    val module: Module

}