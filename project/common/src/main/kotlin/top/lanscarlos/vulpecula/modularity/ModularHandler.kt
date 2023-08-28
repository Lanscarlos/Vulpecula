package top.lanscarlos.vulpecula.modularity

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.modularity
 *
 * @author Lanscarlos
 * @since 2023-08-30 15:01
 */
interface ModularHandler : ModularComponent {

    val bind: List<String>

    fun buildQuest(): BacikalQuest

}