package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestCompiler
import top.lanscarlos.vulpecula.bacikal.quest.DefaultBacikalCompiler
import top.lanscarlos.vulpecula.bacikal.quest.DefaultKetherCompiler
import top.lanscarlos.vulpecula.config.bindConfigSection

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-08-20 22:01
 */
object DefaultBacikalService : BacikalService {

    override val questCompiler: BacikalQuestCompiler by bindConfigSection("bacikal.compiler") { value ->
        when (value) {
            "bacikal" -> DefaultBacikalCompiler
            "kether" -> DefaultKetherCompiler
            else -> throw IllegalArgumentException("Unknown compiler: $value")
        }
    }

}