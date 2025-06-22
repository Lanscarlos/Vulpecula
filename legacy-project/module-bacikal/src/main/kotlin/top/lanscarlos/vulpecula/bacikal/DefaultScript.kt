package top.lanscarlos.vulpecula.bacikal

import top.lanscarlos.vulpecula.bacikal.quest.DefaultQuest
import top.lanscarlos.vulpecula.bacikal.quest.KetherQuest
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-09-03 21:36
 */
class DefaultScript(name: String, override val file: File) : BacikalScript, DefaultQuest(
    Bacikal.service.questCompiler.compile(name, file.readText(StandardCharsets.UTF_8))
)