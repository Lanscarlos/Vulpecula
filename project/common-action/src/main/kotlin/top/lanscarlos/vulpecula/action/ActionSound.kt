package top.lanscarlos.vulpecula.action

import top.lanscarlos.vulpecula.bacikal.bacikalParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.action
 *
 * @author Lanscarlos
 * @since 2024-01-05 00:48
 */
object ActionSound {

    @BacikalParser("sound")
    fun parser() = bacikalParser {
        fructus(
            text(),
            optional("by", then = pair(float(), float()), def = 1f to 1f)
        ) { frame, name, (pitch, volume) ->
        }
    }

}