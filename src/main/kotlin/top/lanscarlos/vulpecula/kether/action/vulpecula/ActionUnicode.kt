package top.lanscarlos.vulpecula.kether.action.vulpecula

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common5.cbool
import taboolib.module.chat.component
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.kether.KetherRegistry
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.*
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-13 16:16
 */
object ActionUnicode {

    val specialDigit by bindConfigNode("action-setting.unicode.special-digit") { value ->
        (value as? List<*>)?.mapNotNull { it?.toString()?.toIntOrNull() }?.sortedDescending() ?: listOf(1, 2, 3, 4, 5, 6, 7, 8, 16, 32, 64, 128).sortedDescending()
    }

    private val unicodePattern = "\\\\u[A-Za-z0-9]{4}".toPattern()
    private val mappingPattern = "@([A-Za-z0-9_\\-+~!@\$%*\\u4e00-\\u9fa5]+)(?=\\b|\\n|\\r)|@\\{([A-Za-z0-9_\\-+~!@\$%*\\u4e00-\\u9fa5]+)}".toPattern()

    private fun String.replaceUnicode(): String {
        if (!this.contains("\\u")) return this

        val matcher = unicodePattern.matcher(this)
        val buffer = StringBuffer()

        try {
            while (matcher.find()) {
                val found = matcher.group()
                val transfer = Integer.parseInt(found.substring(2), 16).toChar()
                matcher.appendReplacement(buffer, transfer.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return matcher.appendTail(buffer).toString()
    }

    private fun String.mapping(): String {
        /*
        * @xxx
        * @{xxx}
        * */
        val matcher = mappingPattern.matcher(this)
        val buffer = StringBuffer()

        while (matcher.find()) {
            val found = matcher.group(1) ?: matcher.group(2) ?: continue
            val mapping = if (found.startsWith('+')) {
                // @{+\d} 正空格
                val target = found.substring(1).toInt()
                splitNumber(target).joinToString("") { mapping["+$it"] ?: "@{+$it}" }
            } else if (found.startsWith('-')) {
                // @{-\d} 负空格
                val target = found.substring(1).toInt()
                splitNumber(target).joinToString("") { mapping["-$it"] ?: "@{-$it}" }
            } else {
                // 其他
                mapping[found] ?: matcher.group()
            }
            matcher.appendReplacement(buffer, mapping)
        }

        return matcher.appendTail(buffer).toString()
    }

    /**
     * 将给定数字分割成一组特定数字之和
     * */
    private fun splitNumber(target: Int): List<Int> {
        val result = mutableListOf<Int>()
        var remaining = target
        var index = 0

        while (remaining > 0) {
            if (index >= specialDigit.size) break
            val part = specialDigit[index]

            if (remaining >= part) {
                result += part
                remaining -= part
            } else {
                index += 1
            }
        }
        return result
    }

    @VulKetherParser(
        id = "unicode",
        name = ["unicode"]
    )
    fun parser() = buildParser { reader ->
        val raw = reader.hasNextToken("raw")
        group(stringOrNull()) {
            now {
                it?.mapping()?.replaceUnicode()?.run {
                    if (raw) this.component() else this
                }
            }
        }
    }

    /**
     * 判断该语句是否启用
     * */
    val enable get() = KetherRegistry.hasAction("unicode")

    val automaticReload by bindConfigNode("automatic-reload.action-unicode") {
        it?.cbool ?: false
    }

    private val folder by lazy { File(getDataFolder(), "actions/unicode") }

    private val mapping by lazy { mutableMapOf<String, String>() }

    private fun onFileChange(file: File) {
        if (!automaticReload) {
            file.removeWatcher()
            return
        }
        try {
            val start = timing()

            mapping.clear()

            file.toConfig().forEachLine { key, value ->
                mapping[key] = value
            }

            console().sendLang("Action-Unicode-Mapping-Load-Succeeded", mapping.size, timing(start))
        } catch (e: Exception) {
            e.printStackTrace()
            console().sendLang("Action-Unicode-Mapping-Load-Failed", e.localizedMessage)
        }
    }

    fun load(): String {
        return try {
            val start = timing()

            // 清除缓存
            mapping.clear()

            folder.ifNotExists {
                releaseResourceFile("actions/unicode/#def.yml", true)
                releaseResourceFile("actions/unicode/#blank.yml", true)
            }.getFiles().forEach { file ->

                file.toConfig().forEachLine { key, value ->
                    // 载入映射列表
                    mapping[key] = value
                }

                if (automaticReload) file.addWatcher { onFileChange(this) }
            }

            console().asLangText("Action-Unicode-Mapping-Load-Succeeded", mapping.size, timing(start)).also {
                console().sendMessage(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            console().asLangText("Action-Unicode-Mapping-Load-Failed", e.localizedMessage).also {
                console().sendMessage(it)
            }
        }
    }

}