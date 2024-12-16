package top.lanscarlos.vulpecula.item

import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.item
 *
 * 基于 NBT 的自定义附魔拓展
 *
 * @author Lanscarlos
 * @since 2024-12-16 00:50
 */
class CustomEnchantment(val id: String, private var config: Configuration) {



    fun update(config: Configuration) {
        this.config = config
    }

}