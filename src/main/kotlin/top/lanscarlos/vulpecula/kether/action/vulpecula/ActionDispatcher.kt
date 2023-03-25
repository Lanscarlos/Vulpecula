package top.lanscarlos.vulpecula.kether.action.vulpecula

import taboolib.library.kether.QuestReader
import taboolib.module.kether.*
import top.lanscarlos.vulpecula.internal.EventDispatcher
import top.lanscarlos.vulpecula.kether.VulKetherParser
import top.lanscarlos.vulpecula.kether.live.readString
import top.lanscarlos.vulpecula.utils.hasNextToken

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action.vulpecula
 *
 * @author Lanscarlos
 * @since 2023-02-09 18:49
 */
@Deprecated("")
object ActionDispatcher {

    fun parse(reader: QuestReader): ScriptAction<*> {
        return reader.switch {
            case("enable", "run") {
                if (reader.hasNextToken("*")) {
                    actionNow {
                        EventDispatcher.cache.values.forEach { it.registerListener() }
                    }
                } else {
                    val id = reader.readString()
                    actionTake {
                        id.getOrNull(this).thenAccept {
                            val dispatcherId = it ?: error("No dispatcher id selected.")
                            EventDispatcher.get(dispatcherId)?.registerListener() ?: error("No dispatcher found: \"${it}\"")
                        }
                    }
                }
            }
            case("disable", "stop") {
                if (reader.hasNextToken("*")) {
                    actionNow {
                        EventDispatcher.cache.values.forEach { it.unregisterListener() }
                    }
                } else {
                    val id = reader.readString()
                    actionTake {
                        id.getOrNull(this).thenAccept {
                            val dispatcherId = it ?: error("No dispatcher id selected.")
                            EventDispatcher.get(dispatcherId)?.unregisterListener() ?: error("No dispatcher found: \"${it}\"")
                        }
                    }
                }
            }
        }
    }

    @VulKetherParser(
        id = "vulpecula-dispatcher",
        name = ["vul-dispatcher"]
    )
    fun parser() = scriptParser { reader ->
        parse(reader)
    }

}