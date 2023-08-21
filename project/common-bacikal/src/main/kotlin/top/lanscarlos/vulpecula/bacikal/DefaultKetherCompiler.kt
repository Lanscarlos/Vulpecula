package top.lanscarlos.vulpecula.bacikal

import taboolib.module.kether.KetherScriptLoader
import taboolib.module.kether.ScriptService
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 23:20
 */
object DefaultKetherCompiler : BacikalCompiler {

    override fun compile(source: String, namespace: List<String>): BacikalQuest {
        return DefaultBacikalQuest(
            KetherScriptLoader().load(
                ScriptService,
                "temp_${UUID.randomUUID()}",
                source.toByteArray(StandardCharsets.UTF_8),
                listOf("vulpecula", *namespace.toTypedArray())
            )
        )
    }

}