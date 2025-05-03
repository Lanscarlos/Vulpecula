package top.lanscarlos.vulpecula.bacikal.quest

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.quest
 *
 * @author Lanscarlos
 * @since 2025-05-03 13:18
 */
class BacikalCompileException(cause: Throwable, val parsedContent: String, val unparseContent: String) : BacikalException(cause) {

    override fun getLocalizedMessage(): String {
        return cause.localizedMessage
    }

}