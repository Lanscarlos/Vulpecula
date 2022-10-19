package top.lanscarlos.vulpecula.kether

import taboolib.library.kether.QuestActionParser

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.kether
 *
 * @author Lanscarlos
 * @since 2022-10-18 18:07
 */
class ParserMetadata(
    val id: String,
    val parser: QuestActionParser,
    val name: Array<String>,
    val namespace: String,
    val shared: Boolean,
    val override: Array<String>,
    val injectDefaultNamespace: Boolean,
    val overrideDefaultAction: Boolean
)