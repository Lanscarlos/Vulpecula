package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.library.kether.Quest
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
    val line = properties["bacikal-line"].let(IntApplicative::convertOrThrow)

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
        require(quest is BacikalQuest)
        val builder = StringBuilder(console().asLangText("module-bacikal-service-execute-failure-detail-header"))
        val startIndex = (line - 2).coerceAtLeast(0)
        for (index in startIndex until startIndex + 5) {
            val color = when {
                index < this.line -> colorParsed
                index == this.line -> colorError
                else -> colorWarning
            }
            val line = quest.lines.getOrNull(index) ?: break
            val content = console().asLangText("module-bacikal-service-execute-failure-detail-item", (index + 1).formatIndex(), color + line)
            builder.append('\n').append(content)
        }
        return builder.toString()
    }

    private fun Int.formatIndex(): String {
        return String.format("%3d", this)
    }

}