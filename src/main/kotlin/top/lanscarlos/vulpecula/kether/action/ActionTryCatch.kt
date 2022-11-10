package top.lanscarlos.vulpecula.kether.action

import taboolib.common.platform.function.console
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.utils.hasNextToken
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.run
import top.lanscarlos.vulpecula.utils.setVariable
import java.util.concurrent.CompletableFuture

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2022-09-02 19:28
 */
class ActionTryCatch(
    val tryBlock: ParsedAction<*>,
    val catchBlock: ParsedAction<*>?,
    val catchTypes: Collection<String>
) : ScriptAction<Any?>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Any?> {
        return try {
            frame.run(tryBlock)
        } catch (e: Exception) {
            e.printStackTrace()
            val exceptionName = e::class.java.simpleName
            console().sendLang("Action-TryCatch-Warning", exceptionName, e.localizedMessage)
            if (catchBlock == null || (catchTypes.isNotEmpty() && exceptionName.uppercase() !in catchTypes)) {
                CompletableFuture.completedFuture(null)
            } else {
                frame.setVariable("error", exceptionName)
                frame.setVariable("exception", exceptionName)
                frame.setVariable("exceptionInfo", e.localizedMessage)
                frame.setVariable("@Exception", e)
                frame.run(catchBlock)
            }
        }
    }

    companion object {

        /**
         *
         * try { ... } catch { ... }
         * try { ... } catch with "NullPointerException|OtherException" { ... }
         *
         * */
        @VulKetherParser(
            id = "try-catch",
            name = ["try"]
        )
        fun parse() = scriptParser { reader ->
            val tryBlock = reader.nextBlock()
            val catchTypes = mutableListOf<String>()

            val catchBlock = if (reader.hasNextToken("catch")) {
                if (reader.hasNextToken("with")) {
                    reader.nextToken().replace("\\s+".toRegex(), " ").split("|").forEach { type ->
                        catchTypes += type.uppercase()
                    }
                }
                reader.nextBlock()
            } else {
                null
            }
            ActionTryCatch(tryBlock, catchBlock, catchTypes)
        }
    }
}