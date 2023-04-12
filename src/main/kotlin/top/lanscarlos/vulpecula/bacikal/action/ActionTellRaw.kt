package top.lanscarlos.vulpecula.bacikal.action

import taboolib.module.chat.component
import taboolib.module.kether.player
import top.lanscarlos.vulpecula.bacikal.BacikalParser
import top.lanscarlos.vulpecula.bacikal.LiveData
import top.lanscarlos.vulpecula.bacikal.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-04-12 09:35
 */
object ActionTellRaw {

    /**
     * tell-raw &message
     * */
    @BacikalParser(
        id = "tell-raw",
        name = ["tell-raw"]
    )
    fun tellRawParser() = bacikal {
        combine(
            text()
        ) { message ->
            message.component().build().sendTo(player())
        }
    }

    /**
     * tell &message
     * tell raw &message
     * */
    @BacikalParser(
        id = "tell",
        name = ["tell*"],
        override = ["tell"]
    )
    fun tellParser() = bacikal {
        combine(
            LiveData.readerOf { !this.expectToken("raw") },
            text()
        ) { legacy, message ->
            if (legacy) {
                player().sendMessage(message)
            } else {
                message.component().build().sendTo(player())
            }
        }
    }

}