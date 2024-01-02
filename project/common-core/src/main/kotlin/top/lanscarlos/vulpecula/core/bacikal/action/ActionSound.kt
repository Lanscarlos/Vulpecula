package top.lanscarlos.vulpecula.core.bacikal.action

import top.lanscarlos.vulpecula.bacikal.bacikalParser
import top.lanscarlos.vulpecula.bacikal.parser.BacikalParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-08-21 19:55
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