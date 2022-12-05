package top.lanscarlos.vulpecula.kether.action

import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.kether.actionTake
import taboolib.module.kether.scriptParser
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.StringLiveData
import top.lanscarlos.vulpecula.utils.*
import java.io.File
import java.util.regex.Pattern
import kotlin.text.StringBuilder

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-11-13 16:16
 */
object ActionUnicode {

    private val folder by lazy { File(getDataFolder(), "actions/unicode") }

    private val fileCache by lazy { mutableSetOf<File>() }
    private val mapping by lazy { mutableMapOf<String, String>() }

    private fun onFileChange(file: File) {
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

            // 移除文件监听器
            fileCache.forEach { it.removeWatcher() }

            // 清除缓存
            mapping.clear()
            fileCache.clear()

            folder.ifNotExists {
                releaseResourceFile("actions/unicode/def-mapping.yml", true)
            }.getFiles().forEach { file ->

                file.toConfig().forEachLine { key, value ->
                    // 载入映射列表
                    mapping[key] = value
                }

                fileCache += file
                file.addWatcher { onFileChange(this) }
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

    private fun String.mappingUnicode(keyword: Char = '@', prefix: Char = '{', suffix: Char = '}'): String {
        val content = this.toCharArray()
        var index = 0
        val builder = StringBuilder()
        while (index < content.size) {

            if (content[index] == keyword) {
                // 检测到起始符
                val key = StringBuilder()
                index++ // 跳过起始符 "$"
                if (index >= content.size) continue // 已检索到字符串末尾
                if (content[index] == prefix) {
                    // 含有拓展符 "{"
                    index++ // 跳过拓展符 "{"
                    while (index < content.size && content[index] != suffix) {
                        // 依次加入变量名缓存
                        key.append(content[index])
                        index++
                    }
                    index++ // 跳过最后的终止符 "}"
                } else {
                    if (Character.isDigit(content[index])) {
                        // 变量名以数字开头，不合法，跳过处理
                        builder.append('$')
                        builder.append(content[index])
                        continue
                    }
                    // 依次将字母或数字加入变量名缓存
                    while (index < content.size && isLetterOrDigit(content[index])) {
                        key.append(content[index])
                        index++
                    }
                }

                // 查询 unicode 映射
                val unicode = mapping[key.toString()]
                if (unicode != null) {
                    // 查询成功
                    builder.append(unicode)
                } else {
                    // 查询失败
                    builder.append('@')
                    builder.append('{')
                    builder.append(key.toString())
                    builder.append('}')
                }
            } else {
                // 非起始符
                builder.append(content[index++])
            }
        }
        return builder.toString()
    }

    private fun isLetterOrDigit(char: Char): Boolean {
        if (char == '_' || char == '-') return true
        val uppercase = 1 shl Character.UPPERCASE_LETTER.toInt()
        val lowercase = 1 shl Character.LOWERCASE_LETTER.toInt()
        val digit = 1 shl Character.DECIMAL_DIGIT_NUMBER.toInt()
        return ((( (uppercase or lowercase) or digit ) shr Character.getType(char.code)) and 1) != 0
    }

    private fun String.replaceUnicode(): String {
        if (!this.contains("\\u")) return this

        val pattern = "\\\\u[A-Za-z0-9]{4}".toPattern()
        val matcher = pattern.matcher(this)
        val builder = StringBuilder()

        try {
            while (matcher.find()) {
                val found = matcher.group()
                val transfer = Character.toString(Integer.parseInt(found.substring(2), 16))
                matcher.appendReplacement(builder, transfer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return matcher.appendTail(builder).toString()
    }

    @VulKetherParser(
        id = "unicode",
        name = ["unicode"]
    )
    fun parser() = scriptParser { reader ->
        val source = StringLiveData(reader.nextBlock())
        actionTake {
            source.getOrNull(this).thenApply {
                it?.mappingUnicode()
            }
        }
    }

}