package top.lanscarlos.vulpecula.bacikal.quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalQuestCompiler {

    fun compile(source: String, namespace: List<String>): BacikalQuest

}