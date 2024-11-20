package top.lanscarlos.vulpecula.bacikal

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2024-11-20 10:41
 */
interface BacikalActionResolver {

    annotation class Action(val id: String)

    annotation class Expected(val prefix: Array<String>)

    annotation class Optional(val prefix: Array<String>)

    annotation class Additional(val prefix: Array<String>)

}