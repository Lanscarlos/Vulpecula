package top.lanscarlos.vulpecula.utils

import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.ConfigFile
import taboolib.module.configuration.Configuration
import java.io.File

inline fun File.ifNotExists(func: ((file: File) -> Unit)): File {
    if (!exists()) func(this)
    return this
}

/**
 * 过滤有效文件
 * */
fun File.getFiles(file : File = this, filter : String = "#", suffix: Array<String> = arrayOf("yml", "yaml")) : List<File> {
    if (!file.exists()) return listOf()
    return mutableListOf<File>().apply {
        // 过滤前缀
        if (file.name.startsWith(filter)) return@apply

        if(file.isDirectory) {
            file.listFiles()?.forEach {
                addAll(getFiles(it))
            }
        } else if (file.extension in suffix) {
            add(file)
        }
    }
}

fun File.toConfig(): ConfigFile {
    return Configuration.loadFromFile(this)
}

/**
 * 加载 Config 下所有 String 内容
 * */
inline fun ConfigurationSection.forEachLine(func: ((key: String, value: String) -> Unit)) {
    getKeys(false).forEach { key ->
        getString(key)?.let { func(key, it) }
    }
}

inline fun <reified T> Configuration.forEachObject(func: ((key: String, value: T) -> Unit)) {
    getKeys(false).forEach { key ->
        val it = get(key)
        if (it is T) func(key, it)
    }
}

/**
 * 加载 Config 下所有 Section 内容
 * */
inline fun Configuration.forEachSections(func: ((key: String, section: ConfigurationSection) -> Unit)) {
    getKeys(false).forEach { key ->
        getConfigurationSection(key)?.let { section ->
            func(key, section)
        }
    }
}

/**
 * 加载该目录下所有子文件的 Section 内容
 * */
inline fun File.deepSections(crossinline func: ((file: File, key: String, section: ConfigurationSection) -> Unit)) {
    this.getFiles().forEach { it.toConfig().forEachSections { key, section -> func(it, key, section) } }
}

inline fun File.addWatcher(runFirst: Boolean = false, crossinline func: (File.() -> Unit)): File {
    if (FileWatcher.INSTANCE.hasListener(this)) return this
    FileWatcher.INSTANCE.addSimpleListener(this, { func(this) }, runFirst)
    return this
}

fun File.removeWatcher() {
    FileWatcher.INSTANCE.removeListener(this)
}