package top.lanscarlos.vulpecula.bacikal.quest

import taboolib.library.kether.Quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-20 22:02
 */
interface BacikalQuest {

    val name: String

    val source: KetherQuest

}

typealias KetherQuest = Quest