package top.lanscarlos.vulpecula.kether

import taboolib.common.platform.function.console
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.KetherParser
import taboolib.module.kether.actionNow
import taboolib.module.kether.scriptParser
import taboolib.module.lang.sendLang
import top.lanscarlos.vulpecula.utils.nextBlock
import top.lanscarlos.vulpecula.utils.run
import top.lanscarlos.vulpecula.utils.setVariable

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
    @KetherParser(["try"], shared = true)
    fun parse() = scriptParser { reader ->
        val tryBlock = reader.nextBlock()
        val catchType = mutableListOf<String>()

        // 解析 catch 语句块
        val catchBlock = try {
            reader.mark()
            reader.expect("catch")
            try {
                reader.mark()
                reader.expect("with")
                reader.nextToken().replace("\\s+".toRegex(), " ").split("|").forEach { type ->
                    catchType += type.uppercase()
                }
            } catch (e: Exception) {
                reader.reset()
            }
            reader.nextBlock()
        } catch (e: Exception) {
            reader.reset()
            null
        }

        actionNow {
            return@actionNow try {
                tryBlock.run(this)
            } catch (e: Exception) {
                val exceptionName = e::class.java.simpleName
                console().sendLang("Action-TryCatch-Warning", exceptionName, e.localizedMessage)
                if (catchBlock == null || (catchType.isNotEmpty() && exceptionName.uppercase() !in catchType)) {
                    null
                } else {
                    this.setVariable("error", exceptionName)
                    this.setVariable("exception", exceptionName)
                    this.setVariable("exceptionInfo", e.localizedMessage)
                    this.setVariable("@Exception", e)
                    catchBlock.run(this)
                }
            }
        }
    }

}