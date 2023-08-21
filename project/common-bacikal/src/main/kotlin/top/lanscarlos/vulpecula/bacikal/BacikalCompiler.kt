package top.lanscarlos.vulpecula.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 21:30
 */
interface BacikalCompiler {

    fun compile(source: String, namespace: List<String>): BacikalQuest

}