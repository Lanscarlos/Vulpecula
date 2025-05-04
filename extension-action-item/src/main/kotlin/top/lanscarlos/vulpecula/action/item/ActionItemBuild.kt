package top.lanscarlos.vulpecula.action.item

import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.platform.util.buildItem
import top.lanscarlos.vulpecula.module.bacikal.annotation.Additional
import top.lanscarlos.vulpecula.module.bacikal.annotation.BacikalParser
import top.lanscarlos.vulpecula.module.bacikal.parser.BacikalActionResolver

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.action.item
 *
 * @author Lanscarlos
 * @since 2024-11-20 20:39
 */
@BacikalParser
object ActionItemBuild : BacikalActionResolver {

    override val id: String = "build"

    override val bind: String = "item"

    fun resolve(
        type: String,
        @Additional(["name"]) name: String? = "wcnm"
    ): ItemStack {
        val material = XMaterial.matchXMaterial(type).orElseThrow { IllegalArgumentException("ActionItemBuild#resolve >> Unknown material: $type") }
        return buildItem(material) {
            if (name != null) {
                this.name = name
            }
            colored()
        }
    }

}