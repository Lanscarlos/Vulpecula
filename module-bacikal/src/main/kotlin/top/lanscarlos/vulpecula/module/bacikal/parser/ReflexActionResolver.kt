package top.lanscarlos.vulpecula.module.bacikal.parser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.parser
 *
 * @author Lanscarlos
 * @since 2024-11-20 10:41
 */
interface ReflexActionResolver {

    /**
     * 绑定主体, 若为空则不绑定
     */
    val bind: String?

    /**
     * 语句 ID
     */
    val id: String

}