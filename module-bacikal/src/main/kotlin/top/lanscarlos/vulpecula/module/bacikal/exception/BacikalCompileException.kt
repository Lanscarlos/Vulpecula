package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.library.kether.ParsedAction
import taboolib.module.chat.colored
import taboolib.module.kether.Kether
import taboolib.module.kether.action.ActionLiteral
import top.lanscarlos.vulpecula.common.applicative.IntApplicative
import top.lanscarlos.vulpecula.common.message.MessageService

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025-05-03 13:18
 */
class BacikalCompileException(
    cause: Throwable,
    private val parsedContent: String,
    private val unparseContent: String,
    private val actions: List<ParsedAction<*>>
) : BacikalException(cause) {

    private val colorParsed: String = "&a".colored()
    private val colorWarning: String = "&e".colored()
    private val colorError: String = "&c".colored()
    private val padding: Int = 2

    override fun getLocalizedMessage(): String {
        return cause.localizedMessage
    }

    /**
     * 获取报错原因信息
     * */
    fun getErrorReasonMessage(): String {
        return MessageService.asLang("module-bacikal-service-compile-failure-reason", localizedMessage)
    }

    /**
     * 获取报错详情信息
     * */
    fun getErrorDetailMessage(): String {
        return when {
            checkLiteralMisspelled() -> {
                // Literal 拼写异常
                buildLiteralMisspelledErrorMessage(parsedContent + unparseContent, actions)
            }
            else -> {
                // 其他异常
                buildCommonErrorMessage(parsedContent, unparseContent)
            }
        }
    }

    private fun checkLiteralMisspelled(): Boolean {
        if (!Kether.isAllowToleranceParser) {
            return false
        }
        for ((index, wrapped) in actions.withIndex()) {
            val action = wrapped.action
            if (action !is ActionLiteral) {
                continue
            }
            if (!action.isMisspelled) {
                continue
            }
            return index != actions.lastIndex
        }
        return false
    }

    private fun buildLiteralMisspelledErrorMessage(source: String, actions: List<ParsedAction<*>>): String {
        val misspelledAction = actions.first {
            val action = it.action
            action is ActionLiteral<*> && action.isMisspelled
        }
        val errorStartIndex = misspelledAction.properties["BACIKAL_START_INDEX"].let(IntApplicative::convertOrThrow)
        val errorEndIndex = misspelledAction.properties["BACIKAL_END_INDEX"].let(IntApplicative::convertOrThrow)
        val startLine = misspelledAction.properties["BACIKAL_START_LINE"].let(IntApplicative::convertOrThrow)
        val endLine = misspelledAction.properties["BACIKAL_END_LINE"].let(IntApplicative::convertOrThrow)

        val errorRange = errorStartIndex..errorEndIndex
        val lines = highlight(source, errorRange).split('\n')
        return buildErrorDetailMessage(lines, startLine..endLine)
    }

    private fun buildCommonErrorMessage(parsedContent: String, unparseContent: String): String {
        val errorStartIndex = parsedContent.lastIndexOf('\n') + 1
        val errorEndIndex = parsedContent.length
        val errorRange = errorStartIndex..errorEndIndex
        val line = parsedContent.count { it == '\n' }
        val lines = highlight(parsedContent + unparseContent, errorRange).split('\n')
        return buildErrorDetailMessage(lines, line, 2)
    }

    private fun highlight(source: String, errorRange: IntRange): String {
        val builder = StringBuilder()
        for ((index, line) in source.substring(0, errorRange.first).split('\n').withIndex()) {
            if (index != 0) {
                builder.append('\n')
            }
            builder.append(colorParsed).append(line)
        }
        for ((index, line) in source.substring(errorRange.first, errorRange.last).split('\n').withIndex()) {
            if (index != 0) {
                builder.append('\n')
            }
            builder.append(colorError).append(line)
        }
        if (errorRange.last == source.length) {
            return builder.toString()
        }
        for ((index, line) in source.substring(errorRange.last, source.length).split('\n').withIndex()) {
            if (index != 0) {
                builder.append('\n')
            }
            builder.append(colorWarning).append(line)
        }
        return builder.toString()
    }

    private fun buildErrorDetailMessage(lines: List<String>, range: IntRange): String {
        val startLine = range.first - padding
        val endLine = range.last + padding + 1
        return buildErrorDetailMessage(lines, startLine.coerceAtLeast(0), endLine.coerceAtMost(lines.size))
    }

    private fun buildErrorDetailMessage(lines: List<String>, startLine: Int, endLine: Int): String {
        val builder = StringBuilder(MessageService.asLang("module-bacikal-service-compile-failure-detail-header"))
        for (index in startLine until endLine) {
            val line = lines.getOrNull(index) ?: break
            val content = MessageService.asLang("module-bacikal-service-compile-failure-detail-item", (index + 1).formatIndex(), line)
            builder.append('\n').append(content)
        }
        return builder.toString()
    }

    private fun Int.formatIndex(): String {
        return String.format("%3d", this)
    }

}