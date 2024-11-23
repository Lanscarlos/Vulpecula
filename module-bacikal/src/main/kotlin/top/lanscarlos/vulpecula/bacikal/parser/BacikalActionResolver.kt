package top.lanscarlos.vulpecula.bacikal.parser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-11-20 10:41
 */
interface BacikalActionResolver {

    /**
     * 语句 ID
     */
    val id: String

    /**
     * 绑定主体, 若为空则不绑定
     */
    val bind: String?

}