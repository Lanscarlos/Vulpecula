package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.BacikalQuestCompiler
import top.lanscarlos.vulpecula.bacikal.quest.FixedQuestCompiler
import top.lanscarlos.vulpecula.bacikal.quest.KetherQuestCompiler
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
            "bacikal" -> FixedQuestCompiler
            "kether" -> KetherQuestCompiler
            else -> throw IllegalArgumentException("Unknown compiler: $value")
        }
    }

}