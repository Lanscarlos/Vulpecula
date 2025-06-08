package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.chat.colored
import taboolib.module.kether.printKetherErrorMessage
import top.lanscarlos.vulpecula.common.applicative.IntApplicative
import top.lanscarlos.vulpecula.common.lang.asLang
import top.lanscarlos.vulpecula.common.lang.error

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025-05-02 10:54
 */
open class BacikalRuntimeException(
    cause: Throwable,
    val quest: Quest,
    properties: Map<String, Any>,
) : BacikalException(cause) {

    override val message: String = asLang("module-bacikal-exception-execute-failure")

    val content = properties["BACIKAL_CONTENT"].toString()
    val startLine = properties["BACIKAL_START_LINE"].let(IntApplicative::convert)
    val endLine = properties["BACIKAL_END_LINE"].let(IntApplicative::convert)

    private val colorParsed: String = "&a".colored()
    private val colorWarning: String = "&e".colored()
    private val colorError: String = "&c".colored()

    fun printLocalizedMessage(sender: ProxyCommandSender) {
        sender.error(sync = true) { getActionMessage() }
        sender.error(sync = true) { getReasonMessage() }
        sender.error(sync = true) { getDetailMessage() }
    }

    fun getActionMessage(): String {
        return asLang("module-bacikal-exception-action", content)
    }

    /**
     * 获取报错原因信息
     * */
    fun getReasonMessage(): String {
        return asLang("module-bacikal-exception-reason", cause.localizedMessage)
    }

    fun getDetailMessage(): String {
        val lines = quest.getProperty<CharArray>("content")?.let(::String)?.split('\n')
        val builder = StringBuilder(asLang("module-bacikal-exception-detail-header"))
        if (lines == null) {
            listOf(
                "&c# 无法查看当前任务的全部源码",
                "&c# Unable to view all source code of the current task."
            ).forEach {
                builder.appendLine(asLang("module-bacikal-exception-detail-item", 1.formatIndex(), it.colored()))
            }
            val footer = asLang("module-bacikal-exception-detail-footer")
            if (footer.isNotBlank()) {
                builder.append('\n').append(footer)
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
            val content = asLang("module-bacikal-exception-detail-item", (index + 1).formatIndex(), color + line)
            builder.append('\n').append(content)
        }
        val footer = asLang("module-bacikal-exception-detail-footer")
        if (footer.isNotBlank()) {
            builder.append('\n').append(footer)
        }
        return builder.toString()
    }

    private fun Int.formatIndex(): String {
        return String.format("%3d", this)
    }

}