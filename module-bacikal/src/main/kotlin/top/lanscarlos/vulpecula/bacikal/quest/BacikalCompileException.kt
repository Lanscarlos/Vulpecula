package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.console
import taboolib.module.chat.colored
import taboolib.module.lang.asLangText

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-03 13:18
 */
class BacikalCompileException(cause: Throwable, parsedContent: String, unparseContent: String) : BacikalException(cause) {

    private val parsedLines: List<String>
    private val unparseLines: List<String>
    private val parsedPart: String
    private val unparsePart: String
    private val startIndex: Int

    private val colorParsed: String = "&a".colored()
    private val colorWarning: String = "&e".colored()
    private val colorError: String = "&c".colored()

    init {
        val parsed = parsedContent.split('\n')
        parsedPart = parsed.lastOrNull() ?: ""
        parsedLines = if (parsed.isNotEmpty()) parsed.takeLast(3).dropLast(1) else emptyList()

        val unparsed = unparseContent.split('\n')
        unparsePart = unparsed.firstOrNull() ?: ""
        unparseLines = if (unparseContent.isNotEmpty()) unparsed.take(3).drop(1) else emptyList()

        startIndex = (parsed.size - 3).coerceAtLeast(0)
    }

    override fun getLocalizedMessage(): String {
        return cause.localizedMessage
    }

    /**
     * 获取报错原因信息
     * */
    fun getErrorReason(): String {
        return console().asLangText("module-bacikal-service-compile-failure-reason", localizedMessage)
    }

    /**
     * 获取报错详情信息
     * */
    fun getErrorDetailMessage(): String {
        val builder = StringBuilder(console().asLangText("module-bacikal-service-compile-failure-detail-header"))
        var index = startIndex + 1
        for (line in parsedLines) {
            val content = console().asLangText("module-bacikal-service-compile-failure-detail-item", index.formatIndex(), colorParsed + line)
            builder.append('\n').append(content)
            index++
        }
        builder.append('\n')
            .append(console().asLangText("module-bacikal-service-compile-failure-detail-item", index.formatIndex(), colorWarning + parsedPart))
            .append(colorError + unparsePart)
        for (line in unparseLines) {
            index++
            val content = console().asLangText("module-bacikal-service-compile-failure-detail-item", index.formatIndex(), colorError + line)
            builder.append('\n').append(content)
        }

        return builder.toString()
    }

    private fun Int.formatIndex(): String {
        return String.format("%3d", this)
    }

}