package top.lanscarlos.vulpecula.bacikal

import taboolib.library.kether.QuestActionParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.bacikal
 *
 * @author Lanscarlos
 * @since 2023-03-26 14:06
 */
class RegistryMetadata(
    val id: String,
    val parser: QuestActionParser,
    val name: Array<String>,
    val namespace: String,
    val shared: Boolean,
    val override: Array<String>,
    val injectDefaultNamespace: Boolean,
    val overrideDefaultAction: Boolean
)