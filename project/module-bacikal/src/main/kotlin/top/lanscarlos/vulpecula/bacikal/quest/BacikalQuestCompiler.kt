package top.lanscarlos.vulpecula.bacikal.quest

import top.lanscarlos.vulpecula.bacikal.Bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalQuestCompiler {

    fun compile(name: String, source: String, namespace: List<String> = Bacikal.service.compileNamespace): BacikalQuest

}