package top.lanscarlos.vulpecula.applicative

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.applicative
 *
 * @author Lanscarlos
 * @since 2024-05-15 20:31
 */

/**
 * 将对象转换为 Boolean LiveData
 *
 * @param def 默认值
 * */
fun Any?.liveBoolean(def: Boolean = false): LiveData<Boolean> {
    return DefaultLiveData(this ?: def, ApplicativeRegistry.getApplicative(Boolean::class.java) ?: BooleanApplicative)
}

/**
 * 将对象转换为 Int LiveData
 *
 * @param def 默认值
 * */
fun Any?.liveInt(def: Int = 0): LiveData<Int> {
    return DefaultLiveData(this ?: def, ApplicativeRegistry.getApplicative(Int::class.java) ?: IntApplicative)
}

/**
 * 将对象转换为 Long LiveData
 *
 * @param def 默认值
 * */
fun Any?.liveLong(def: Long = 0L): LiveData<Long> {
    return DefaultLiveData(this ?: def, ApplicativeRegistry.getApplicative(Long::class.java) ?: LongApplicative)
}

/**
 * 将对象转换为 Float LiveData
 *
 * @param def 默认值
 * */
fun Any?.liveFloat(def: Float = 0f): LiveData<Float> {
    return DefaultLiveData(this ?: def, ApplicativeRegistry.getApplicative(Float::class.java) ?: FloatApplicative)
}

/**
 * 将对象转换为 Double LiveData
 *
 * @param def 默认值
 * */
fun Any?.liveDouble(def: Double = 0.0): LiveData<Double> {
    return DefaultLiveData(this ?: def, ApplicativeRegistry.getApplicative(Double::class.java) ?: DoubleApplicative)
}