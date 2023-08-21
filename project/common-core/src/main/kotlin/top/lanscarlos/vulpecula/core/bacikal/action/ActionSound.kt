package top.lanscarlos.vulpecula.core.bacikal.action

import top.lanscarlos.vulpecula.bacikal.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core.bacikal.action
 *
 * @author Lanscarlos
 * @since 2023-08-21 19:55
 */
object ActionSound {

    fun parser() = bacikal {
        fructus(
            text(),
            expect("by", then = pair(float(), float()))
        ) { frame, name, (pitch, volume) ->
        }
    }
}