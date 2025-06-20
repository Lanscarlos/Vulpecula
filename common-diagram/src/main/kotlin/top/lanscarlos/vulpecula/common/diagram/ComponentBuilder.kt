package top.lanscarlos.vulpecula.common.diagram

import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.common.diagram
 *
 * @author Lanscarlos
 * @since 2025/6/20 17:02
 */
class ComponentBuilder {

    private val plainTextBuilder = StringBuilder()

    private val componentBuilder = Components.empty()

    fun append(text: String): ComponentBuilder {
        plainTextBuilder.append(text)
        return this
    }

    fun append(component: ComponentText): ComponentBuilder {
        if (plainTextBuilder.isNotEmpty()) {
            componentBuilder.append(Components.text(plainTextBuilder.toString()))
            plainTextBuilder.clear()
        }
        componentBuilder.append(component)
        return this
    }

    fun build(): ComponentText {
        if (plainTextBuilder.isNotEmpty()) {
            componentBuilder.append(Components.text(plainTextBuilder.toString()))
            plainTextBuilder.clear()
        }
        return componentBuilder
    }

}