package top.lanscarlos.vulpecula.common.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.applicative
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:45
 */
interface MutableLiveData<T> : LiveData<T> {

    /**
     * 设置属性
     * */
    operator fun set(key: String, value: Any?)

}