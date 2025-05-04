package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.console
import taboolib.library.kether.Quest
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.chat.colored
import taboolib.module.kether.printKetherErrorMessage
import taboolib.module.lang.asLangText
import top.lanscarlos.vulpecula.common.applicative.IntApplicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-02 10:54
 */
open class BacikalRuntimeException(
    cause: Throwable,
    val quest: Quest,
    properties: Map<String, Any>,
) : BacikalException(cause) {

    override val message: String = cause.message ?: "EXCEPTION_MESSAGE_MISSING"

    val header = properties["bacikal-header"].toString()
    val location = properties["bacikal-content"].toString()
    val startLine = properties["bacikal-start-line"].let(IntApplicative::convertOrThrow)
    val endLine = properties["bacikal-end-line"].let(IntApplicative::convertOrThrow)

    private val colorParsed: String = "&a".colored()
    private val colorWarning: String = "&e".colored()
    private val colorError: String = "&c".colored()

    open fun printKetherMessage(detailError: Boolean = false) {
        cause.printKetherErrorMessage(detailError)
    }

    fun getErrorActionMessage(): String {
        return console().asLangText("module-bacikal-service-execute-failure-action", location)
    }

    /**
     * 获取报错原因信息
     * */
    fun getErrorReasonMessage(): String {
        return console().asLangText("module-bacikal-service-execute-failure-reason", localizedMessage)
    }

    fun getErrorDetailMessage(): String {
        val lines = quest.getProperty<CharArray>("content")?.let(::String)?.split('\n')
        val builder = StringBuilder(console().asLangText("module-bacikal-service-execute-failure-detail-header"))
        if (lines == null) {
            listOf(
                "&c# 无法查看当前任务的全部源码",
                "&c# Unable to view all source code of the current task."
            ).forEach {
                builder.appendLine(console().asLangText("module-bacikal-service-execute-failure-detail-item", 1.formatIndex(), it.colored()))
            }
            return builder.toString()
        }
        val startIndex = (startLine - 2).coerceAtLeast(0)
        for (index in startIndex until endLine + 3) {
            val color = when {
                index < startLine -> colorParsed
                index in startLine..endLine -> colorError
                else -> colorWarning
            }
            val line = lines.getOrNull(index) ?: break
            val content = console().asLangText("module-bacikal-service-execute-failure-detail-item", (index + 1).formatIndex(), color + line)
            builder.append('\n').append(content)
        }
        return builder.toString()
    }

    private fun Int.formatIndex(): String {
        return String.format("%3d", this)
    }

}