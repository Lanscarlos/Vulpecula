package top.lanscarlos.vulpecula.common.config

import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigLoader

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2025-03-11 16:53
 */

/**
 * 从 ConfigurationSection 读取指定字段数据
 * */
fun ConfigurationSection.read(vararg keys: String): LiveData<Any?> {
    return DelegateConfigNode(this, keys)
}

/**
 * 绑定 TabooLib 配置文件中的指定键值
 *
 * @param path 键
 * @param bind 文件名
 * */
fun bindConfig(path: String, bind: String = "config.yml"): LiveData<Any?> {
    val configFile = ConfigLoader.files[bind] ?: error("Config $bind not found.")
    return configFile.configuration.read(path)
}