package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.livedata.LiveData

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:42
 */
interface ConfigNode: LiveData<Any?> {

    /**
     * 可能的键
     * */
    val keys: Array<out String>

    /**
     * 当前键
     * */
    val key: String

    /**
     * 路径
     * */
    val path: String

}