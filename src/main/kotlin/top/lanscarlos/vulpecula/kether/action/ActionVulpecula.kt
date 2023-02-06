package top.lanscarlos.vulpecula.kether.action

import taboolib.module.kether.scriptParser
import taboolib.module.kether.switch
import top.lanscarlos.vulpecula.kether.VulKetherParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether.action
 *
 * @author Lanscarlos
 * @since 2023-02-06 15:16
 */
object ActionVulpecula {

    @VulKetherParser(
        id = "vul-script",
        name = ["vulpecula", "vul"]
    )
    fun parser() = scriptParser { reader ->
        reader.switch {
            case("script") { ActionScript.parse(reader) }
        }
    }

}