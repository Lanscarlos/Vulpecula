package top.lanscarlos.vulpecula.common.config

import taboolib.module.configuration.Configuration
import java.io.File
import java.io.InputStream
import java.io.Reader

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.config
 *
 * @author Lanscarlos
 * @since 2024-12-16 14:29
 */
class DefaultSource(val configuration: Configuration) : DefaultSection(configuration), ConfigSource, Runnable {

    constructor(file: File) : this(Configuration.loadFromFile(file))

    constructor(contents: String) : this(Configuration.loadFromString(contents))

    constructor(reader: Reader) : this(Configuration.loadFromReader(reader))

    constructor(inputStream: InputStream) : this(Configuration.loadFromInputStream(inputStream))

    init {
        configuration.onReload(this)
    }

    override fun run() {
        sections.forEach { (_, node) -> node.update() }
    }

    override fun reload() {
        configuration.reload()
    }

    override fun reloadFromFile(file: File) {
        configuration.loadFromFile(file)
    }

    override fun reloadFromString(contents: String) {
        configuration.loadFromString(contents)
    }

    override fun reloadFromReader(reader: Reader) {
        configuration.loadFromReader(reader)
    }

    override fun reloadFromInputStream(inputStream: InputStream) {
        configuration.loadFromInputStream(inputStream)
    }

}