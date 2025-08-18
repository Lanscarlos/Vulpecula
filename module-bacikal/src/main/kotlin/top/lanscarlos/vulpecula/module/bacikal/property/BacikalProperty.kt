package top.lanscarlos.vulpecula.module.bacikal.property

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.property
 *
 * @author Lanscarlos
 * @since 2025/8/3
 */
interface BacikalProperty<T: Any> {

    /**
     * 获取属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * */
    fun readProperty(instance: T, key: String): Any?

    /**
     * 设置属性
     *
     * @param key 属性名, 递归获取属性使用 . 分隔
     * @param value 属性值
     * */
    fun writeProperty(instance: T, key: String, value: Any?)

}