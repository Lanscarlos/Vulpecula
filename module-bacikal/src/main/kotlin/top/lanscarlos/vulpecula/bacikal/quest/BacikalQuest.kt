package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.Quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-03 21:19
 */
class BacikalQuest(native: Quest, val source: String) : Quest by native {

    val lines: List<String> = source.split('\n')

}