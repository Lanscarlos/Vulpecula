package top.lanscarlos.vulpecula.module.bacikal.reflex

import taboolib.common.io.digest
import taboolib.common.platform.function.info
import taboolib.library.reflex.ClassAnnotation
import java.io.File
import java.io.RandomAccessFile

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.reflex
 *
 * @author Lanscarlos
 * @since 2025/6/15
 */
@Metadata
object MetadataParser {

    private val folder: File = File("./cache/taboolib/top.lanscarlos.vulpecula/metadata")

    fun parse(metadata: ClassAnnotation): Pair<Array<String>, Array<String>> {
        if (folder.exists()) {
            return KotlinMetadataFileStorage.loadMetadata("./cache/taboolib/top.lanscarlos.vulpecula/metadata/metadata.ktmd")
        }
        val data1 = metadata.list<String>("d1").toTypedArray()
        val data2 = metadata.list<String>("d2").toTypedArray()
        folder.mkdirs()
        KotlinMetadataFileStorage.saveMetadata(data1, data2, "./cache/taboolib/top.lanscarlos.vulpecula/metadata/metadata.ktmd")
        return data1 to data2
    }

    fun save() {

    }

}