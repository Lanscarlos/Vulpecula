package top.lanscarlos.vulpecula.module.bacikal.reflex

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Vulpecula
 * top.lanscarlos.vulpecula.module.bacikal.reflex
 *
 * @author Lanscarlos
 * @since 2025/6/15
 */
object KotlinMetadataFileStorage {

    private const val SIGNATURE = "KTMDv1"
    private const val HEADER_SIZE = 26 // 6(sig) + 4(d1Cnt) + 4(d2Cnt) + 8(reserved) + 4(headerCrc)

    // 存储 d1 和 d2 到单个文件
    fun saveMetadata(d1: Array<String>, d2: Array<String>, filePath: String) {
        RandomAccessFile(filePath, "rw").use { file ->
            val crc = CRC32()
            val headerBuf = ByteBuffer.allocate(HEADER_SIZE)
            headerBuf.order(ByteOrder.BIG_ENDIAN)

            // 1. 写入文件头签名
            headerBuf.put(SIGNATURE.toByteArray())

            // 2. 写入元素数量
            headerBuf.putInt(d1.size)
            headerBuf.putInt(d2.size)

            // 3. 保留字段（8字节）
            headerBuf.putLong(0L)

            // 4. 计算文件头CRC（暂时空白）
            headerBuf.putInt(0)

            // 写入文件头
            file.write(headerBuf.array())

            // 5. 写入d1元数据和数据
            writeDataBlock(file, d1, crc)

            // 6. 写入d2元数据和数据
            writeDataBlock(file, d2, crc)

            // 7. 计算并更新文件头CRC
            val headerCrc = calculateCrc(headerBuf.array(), 0, HEADER_SIZE - 4)
            file.seek((HEADER_SIZE - 4).toLong()) // 定位到CRC字段位置
            file.writeInt(headerCrc.toInt())

            // 8. 写入整体文件CRC
            val fileSize = file.length()
            val fileData = ByteArray(fileSize.toInt())
            file.seek(0)
            file.readFully(fileData)
            val fullCrc = calculateCrc(fileData)
            file.writeInt(fullCrc.toInt())
        }
    }

    // 从文件加载 d1 和 d2
    fun loadMetadata(filePath: String): Pair<Array<String>, Array<String>> {
        RandomAccessFile(filePath, "r").use { file ->
            // 1. 读取文件头
            val header = ByteArray(HEADER_SIZE)
            file.readFully(header)

            // 验证签名
            val signature = String(header, 0, 6)
            if (signature != SIGNATURE) {
                throw IllegalArgumentException("无效的文件格式")
            }

            val buf = ByteBuffer.wrap(header).order(ByteOrder.BIG_ENDIAN)
            buf.position(6)
            val d1Count = buf.int
            val d2Count = buf.int
            buf.position(HEADER_SIZE) // 跳过保留字段

            // 验证文件头CRC
            val headerCrc = buf.int
            val calcHeaderCrc = calculateCrc(header, 0, HEADER_SIZE - 4)
            if (headerCrc.toLong() != calcHeaderCrc) {
                throw IllegalArgumentException("文件头损坏")
            }

            // 2. 读取d1数据和元数据
            val d1Data = readDataBlock(file, d1Count)

            // 3. 读取d2数据和元数据
            val d2Data = readDataBlock(file, d2Count)

            // 4. 验证整体文件CRC
            val storedFullCrc = file.readInt().toLong() and 0xFFFFFFFFL
            file.seek(0)
            val fileLength = file.length() - 4 // 排除最后的CRC
            val fileData = ByteArray(fileLength.toInt())
            file.readFully(fileData)
            val calcFullCrc = calculateCrc(fileData)

            if (storedFullCrc != calcFullCrc) {
                throw IllegalArgumentException("文件数据损坏")
            }

            return Pair(d1Data, d2Data)
        }
    }

    // 辅助方法：写入数据块（包含长度元数据和实际数据）
    private fun writeDataBlock(file: RandomAccessFile, data: Array<String>, crc: CRC32) {
        // 写入长度数组
        val lengths = IntArray(data.size) { index ->
            data[index].toByteArray(Charsets.ISO_8859_1).size.also { size ->
                file.writeInt(size)
                crc.update(byteArrayOf((size shr 24).toByte(), (size shr 16).toByte(),
                    (size shr 8).toByte(), size.toByte()))
            }
        }

        // 写入实际数据
        data.forEachIndexed { index, str ->
            val bytes = str.toByteArray(Charsets.ISO_8859_1)
            file.write(bytes)
            crc.update(bytes)
        }
    }

    // 辅助方法：读取数据块
    private fun readDataBlock(file: RandomAccessFile, count: Int): Array<String> {
        // 读取长度数组
        val lengths = IntArray(count) { file.readInt() }

        // 读取实际数据
        return Array(count) { index ->
            val buffer = ByteArray(lengths[index])
            file.readFully(buffer)
            String(buffer, Charsets.ISO_8859_1)
        }
    }

    // 计算CRC32校验和
    private fun calculateCrc(data: ByteArray, offset: Int = 0, length: Int = data.size): Long {
        val crc = CRC32()
        crc.update(data, offset, length)
        return crc.value
    }

}