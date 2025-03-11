package top.lanscarlos.vulpecula.common.config

import top.lanscarlos.vulpecula.common.livedata.DefaultLiveData

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 01:48
 */
class DefaultNode(
    val section: ConfigSection,
    override val keys: Array<out String>,
) : ConfigNode {

    override lateinit var key: String

    override val path: String
        get() = section.path + "." + key

    val liveData = DefaultLiveData(source = ::read, transformer = ::transformer)

    private fun read(): Any? {
        for (key in keys) {
            if (!section.contains(key)) {
                continue
            }
            this.key = key
            return section[key]
        }
        return null
    }

    private fun transformer(value: Any?): Any? {
        return value
    }

    override fun getValue(): Any? {
        return liveData.getValue()
    }

    override fun getValueOrNull(): Any? {
        return liveData.getValueOrNull()
    }

    override fun update() {
        liveData.update()
    }

}