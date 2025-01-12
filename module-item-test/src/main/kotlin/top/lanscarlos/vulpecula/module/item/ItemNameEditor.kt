package top.lanscarlos.vulpecula.module.item

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * @author Lanscarlos
 * @since 2024-12-14 16:32
 */
object ItemNameEditor {

    fun append(source: ItemSource, name: String) {
        val item = source.get() ?: return
        source.set(item)
    }

}