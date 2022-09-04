package top.lanscarlos.vulpecula.kether

import taboolib.common.platform.function.console
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.utils.run
import top.lanscarlos.vulpecula.utils.variable

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-09-02 19:28
 */
object ActionTryCatch {

    /**
     *
     * try { ... } catch { ... }
     * try { ... } catch with "NullPointerException|OtherException" { ... }
     *
     * */
    @KetherParser(["try"], namespace = "vulpecula", shared = true)
    fun parse() = scriptParser {
        val tryAction = it.next(ArgTypes.ACTION)
        val catchType = mutableListOf<String>()
        var catchAction: ParsedAction<*>? = null

        try {
            it.mark()
            it.expect("catch")
            try {
                it.mark()
                it.expect("with")
                it.nextToken().replace("\\s+".toRegex(), " ").split("|").forEach { type ->
                    catchType += type.uppercase()
                }
            } catch (e: Exception) {
                it.reset()
            }
            catchAction = it.next(ArgTypes.ACTION)
        } catch (e: Exception) {
            it.reset()
        }

        actionNow {
            return@actionNow try {
                tryAction.run(this)
            } catch (e: Exception) {
                val exceptionName = e::class.java.simpleName
                console().sendLang("Action-TryCatch-Warning", exceptionName, e.localizedMessage)
                if (catchAction == null || (catchType.isNotEmpty() && exceptionName.uppercase() !in catchType)) {
                    null
                } else {
                    this.variable("error", exceptionName)
                    this.variable("exception", exceptionName)
                    this.variable("exceptionInfo", e.localizedMessage)
                    this.variable("@Exception", e)
                    catchAction.run(this)
                }
            }
        }
    }

}