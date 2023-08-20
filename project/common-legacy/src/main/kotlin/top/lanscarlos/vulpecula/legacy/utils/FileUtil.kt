package top.lanscarlos.vulpecula.legacy.utils

import taboolib.common5.FileWatcher
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

fun File.toConfig(): Configuration {
    return Configuration.loadFromFile(this)
}

inline fun File.addWatcher(runFirst: Boolean = false, crossinline func: (File.() -> Unit)): File {
    if (FileWatcher.INSTANCE.hasListener(this)) return this
    FileWatcher.INSTANCE.addSimpleListener(this, { func(this) }, runFirst)
    return this
}

fun File.removeWatcher() {
    FileWatcher.INSTANCE.removeListener(this)
}