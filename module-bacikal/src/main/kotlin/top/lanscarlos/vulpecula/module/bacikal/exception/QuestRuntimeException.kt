package top.lanscarlos.vulpecula.module.bacikal.exception

import taboolib.common.platform.ProxyCommandSender
import taboolib.library.kether.Quest
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.chat.colored
import taboolib.module.lang.sendErrorMessage
import top.lanscarlos.vulpecula.common.applicative.IntApplicative
import top.lanscarlos.vulpecula.common.exception.AbstractLocalizedException
import top.lanscarlos.vulpecula.common.lang.Lang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.exception
 *
 * @author Lanscarlos
 * @since 2025-05-02 10:54
 */
open class QuestRuntimeException(
    override val cause: Throwable,
    val quest: Quest,
    properties: Map<String, Any>,
) : AbstractLocalizedException() {

    override val lang: Lang = Lang.EXCEPTION_QUEST_EXECUTE_FAILURE

    override val arguments: Array<Any> = arrayOf()

    val content = properties["BACIKAL_CONTENT"].toString()
    val startLine = properties["BACIKAL_START_LINE"].let(IntApplicative::convert)
    val endLine = properties["BACIKAL_END_LINE"].let(IntApplicative::convert)

    private val colorParsed: String = "&a".colored()
    private val colorWarning: String = "&e".colored()
    private val colorError: String = "&c".colored()

    override fun notice(receiver: ProxyCommandSender) {
        Lang.EXCEPTION_QUEST_ACTION.error(receiver, content)
        Lang.EXCEPTION_QUEST_REASON.error(receiver, cause.localizedMessage)
        receiver.sendErrorMessage(getDetailMessage(receiver))
    }

    fun getDetailMessage(receiver: ProxyCommandSender): String {
        val lines = quest.getProperty<CharArray>("content")?.let(::String)?.split('\n')
        val builder = StringBuilder(Lang.EXCEPTION_QUEST_LOCATION_HEADER.asText(receiver))
        if (lines == null) {
            listOf(
                "&c# 无法查看当前任务的全部源码",
                "&c# Unable to view all source code of the current task."
            ).forEach {
                builder.appendLine(Lang.EXCEPTION_QUEST_LOCATION_BODY.asText(receiver, 1.formatIndex(), it.colored()))
            }
            val footer = Lang.EXCEPTION_QUEST_LOCATION_FOOTER.asText(receiver)
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
            val content = Lang.EXCEPTION_QUEST_LOCATION_BODY.asText(receiver, (index + 1).formatIndex(), color + line)
            builder.append('\n').append(content)
        }
        val footer = Lang.EXCEPTION_QUEST_LOCATION_FOOTER.asText(receiver)
        if (footer.isNotBlank()) {
            builder.append('\n').append(footer)
        }
        return builder.toString()
    }

    private fun Int.formatIndex(): String {
        return String.format("%3d", this)
    }

}