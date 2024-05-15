package top.lanscarlos.vulpecula.core

import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.core
 *
 * @author Lanscarlos
 * @since 2023-08-27 13:28
 */
object ClassAliases {

    @Config("class-aliases.yml")
    lateinit var classAliases: Configuration
        private set

    fun getClass(name: String): Class<*>? {
        val className = if (!name.contains('.')) {
            classAliases.getString(name) ?: let {
                console().sendLang("Class-Aliases-Not-Found", name)
                return null
            }
        } else {
            name
        }

        return try {
            Class.forName(className)
        } catch (ex: Exception) {
            console().sendLang("Class-Not-Found", className, ex.localizedMessage)
            null
        }
    }
}