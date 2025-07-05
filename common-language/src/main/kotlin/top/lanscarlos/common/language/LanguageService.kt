package top.lanscarlos.common.language

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.console
import taboolib.module.chat.colored
import taboolib.module.lang.asLangText
import taboolib.module.lang.sendLang
import java.util.function.Supplier

/**
 * Vulpecula
 * top.lanscarlos.common.language
 *
 * @author Lanscarlos
 * @since 2025/7/5
 */
object LanguageService {

    private val COLOR_RESET = "&7".colored()

    private val COLOR_INFO = "&a".colored()

    private val COLOR_WARNING = "&e".colored()

    private val COLOR_ERROR = "&c".colored()

    private val console = console()

    fun info(sender: ProxyCommandSender, module: String, sync: Boolean, message: Supplier<Message>) {
        sendLangTo(sender, module, sync, COLOR_INFO, message)
    }

    fun warning(sender: ProxyCommandSender, module: String, sync: Boolean, message: Supplier<Message>) {
        sendLangTo(sender, module, sync, COLOR_WARNING, message)
    }

    fun error(sender: ProxyCommandSender, module: String, sync: Boolean, message: Supplier<Message>) {
        sendLangTo(sender, module, sync, COLOR_ERROR, message)
    }

    fun sendLangTo(sender: ProxyCommandSender, module: String, sync: Boolean, color: String, message: Supplier<Message>) {
        val text = COLOR_RESET + asLangText(sender, color, message.get())
        sender.sendLang("common-language-message", color + module, text)
        if (sync && sender is ProxyPlayer) {
            console.sendLang("common-language-message", color + module, text)
        }
    }

    private fun asLangText(sender: ProxyCommandSender, color: String, message: Message): String {
        val args = message.args.map {
            when (it) {
                is Pair<*, *> -> (color + it.second.toString() + COLOR_RESET) to it.first // 反转键值对
                else -> color + it.toString() + COLOR_RESET
            }
        }.toTypedArray()

        if (message.child == null) {
            return sender.asLangText(message.node, *args)
        }
        val childMessage = asLangText(sender, color, message.child)
        return sender.asLangText(message.node, *args.plus(childMessage))
    }

}