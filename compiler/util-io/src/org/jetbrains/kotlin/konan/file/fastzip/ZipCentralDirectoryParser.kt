/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.file.fastzip

import java.nio.ByteBuffer
import java.util.zip.Inflater

internal class ZipEntryDescription(
    val relativePath: CharSequence,
    val compressedSize: Long,
    val uncompressedSize: Long,
    val offsetInFile: Long,
    val compressionKind: CompressionKind,
    val fileNameSize: Int,
) {
    enum class CompressionKind {
        PLAIN, DEFLATE
    }

    val isDirectory: Boolean get() = uncompressedSize == 0L
}

private const val END_OF_CENTRAL_DIR_SIZE = 22
private const val END_OF_CENTRAL_DIR_ZIP64_SIZE = 56
private const val LOCAL_FILE_HEADER_EXTRA_OFFSET = 28
private const val LOCAL_FILE_HEADER_SIZE = LOCAL_FILE_HEADER_EXTRA_OFFSET + 2

internal fun LargeDynamicMappedBuffer.contentsToByteArray(
    zipEntryDescription: ZipEntryDescription
): ByteArray =
    withMappedRangeFrom(zipEntryDescription.offsetInFile) {
        val extraSize = getUnsignedShort(LOCAL_FILE_HEADER_EXTRA_OFFSET)
        val startPos = LOCAL_FILE_HEADER_SIZE + zipEntryDescription.fileNameSize + extraSize

        require(zipEntryDescription.compressedSize - startPos < Int.MAX_VALUE && zipEntryDescription.uncompressedSize <= Int.MAX_VALUE) {
            "Reading files bigger than Int.MAX_VALUE is not supported yet"
        }

        when (zipEntryDescription.compressionKind) {
            ZipEntryDescription.CompressionKind.DEFLATE -> {
                val inflater = Inflater(true)
                val compressedSize = zipEntryDescription.compressedSize.toInt()
                val setInput = setInflaterInputFromBuffer
                if (setInput != null) {
                    setInput(inflater, slicedBuffer(startPos, compressedSize))
                } else {
                    inflater.setInput(getBytes(startPos, compressedSize))
                }

                val result = ByteArray(zipEntryDescription.uncompressedSize.toInt())
                inflater.inflate(result)
                inflater.end()

                result
            }

            ZipEntryDescription.CompressionKind.PLAIN -> getBytes(startPos, zipEntryDescription.compressedSize.toInt())
        }
    }

/**
 * Zero-copy direct buffer read for uncompressed (PLAIN / STORED) ZIP entries.
 * Returns null if the entry is compressed (caller should fall back to [contentsToByteArray]).
 */
internal fun <R> LargeDynamicMappedBuffer.withDirectBuffer(
    zipEntryDescription: ZipEntryDescription,
    block: (ByteBuffer) -> R
): R? {
    if (zipEntryDescription.compressionKind != ZipEntryDescription.CompressionKind.PLAIN) {
        return null
    }
    return withMappedRangeFrom(zipEntryDescription.offsetInFile) {
        val extraSize = getUnsignedShort(LOCAL_FILE_HEADER_EXTRA_OFFSET)
        val startPos = LOCAL_FILE_HEADER_SIZE + zipEntryDescription.fileNameSize + extraSize
        val size = zipEntryDescription.uncompressedSize.toInt()
        val slice = slicedBuffer(startPos, size)
        block(slice)
    }
}

internal fun LargeDynamicMappedBuffer.parseCentralDirectory(): List<ZipEntryDescription> {
    val (entriesNumber, offsetOfCentralDirectory) = parseCentralDirectoryRecordsNumberAndOffset()

    var currentStart = offsetOfCentralDirectory
    val result = mutableListOf<ZipEntryDescription>()

    for (i in 0 until entriesNumber) {
        withMappedRangeFrom(currentStart) {
            val headerConst = getInt(0)
            require(headerConst == 0x02014b50) {
                "Invalid central directory record signature at index $i: $headerConst"
            }

            val compressionMethod = getShort(10).toInt()
            val fileNameLength = getUnsignedShort(28)
            val extraLength = getUnsignedShort(30)
            val extraFieldOffset = 46 + fileNameLength

            val compressedSize32 = getInt(20)
            val uncompressedSize32 = getInt(24)
            val fileCommentLength = getUnsignedShort(32)

            val offsetOfFileData32 = getInt(42)

            var extraFieldNo = 0
            val extraFieldsSize = getShort(extraFieldOffset + 2)

            fun Int.toLongOrNextZip64ExtrField(): Long =
                if (this != -1) toUInt().toLong()
                else {
                    require(extraFieldsSize >= (extraFieldNo + 1) * 8)
                    getLong(extraFieldOffset + 4 + extraFieldNo * 8).also { extraFieldNo++ }
                }

            val compressedSize = compressedSize32.toLongOrNextZip64ExtrField()
            val uncompressedSize = uncompressedSize32.toLongOrNextZip64ExtrField()
            val offsetOfFileData = offsetOfFileData32.toLongOrNextZip64ExtrField()

            val bytesForName = getBytes(46, fileNameLength)
            val name = if (bytesForName.all { it >= 0 }) {
                ByteArrayCharSequence(bytesForName)
            } else {
                String(bytesForName, Charsets.UTF_8)
            }
            currentStart += 46 + fileNameLength + extraLength + fileCommentLength

            val compressionKind = when (compressionMethod) {
                0 -> ZipEntryDescription.CompressionKind.PLAIN
                8 -> ZipEntryDescription.CompressionKind.DEFLATE
                else -> error("Unexpected compression method ($compressionMethod) at $name")
            }

            result += ZipEntryDescription(
                name, compressedSize, uncompressedSize, offsetOfFileData, compressionKind,
                fileNameLength
            )
        }
    }

    return result
}

private fun LargeDynamicMappedBuffer.parseCentralDirectoryRecordsNumberAndOffset(): Pair<Long, Long> =
    withMappedTail {
        var endOfCentralDirectoryOffset = endOffset() - END_OF_CENTRAL_DIR_SIZE
        while (endOfCentralDirectoryOffset >= 0) {
            if (getInt(endOfCentralDirectoryOffset) == 0x06054b50) break
            endOfCentralDirectoryOffset--
        }

        val entriesNumber = getUnsignedShort(endOfCentralDirectoryOffset + 10)
        val offsetOfCentralDirectory = getInt(endOfCentralDirectoryOffset + 16)
        if (entriesNumber == 0xffff || offsetOfCentralDirectory == -1) {
            parseZip64CentralDirectoryRecordsNumberAndOffset()
        } else {
            Pair(entriesNumber.toLong(), offsetOfCentralDirectory.toUInt().toLong())
        }
    }

private val setInflaterInputFromBuffer: ((Inflater, ByteBuffer) -> Unit)? = run {
    try {
        val method = Inflater::class.java.getMethod("setInput", ByteBuffer::class.java)
        val callback: (Inflater, ByteBuffer) -> Unit = { inflater, buffer -> method.invoke(inflater, buffer) }
        callback
    } catch (_: Throwable) {
        null
    }
}

private fun LargeDynamicMappedBuffer.Mapping.parseZip64CentralDirectoryRecordsNumberAndOffset(): Pair<Long, Long> {
    var endOfCentralDirectoryOffset = endOffset() - END_OF_CENTRAL_DIR_ZIP64_SIZE
    while (endOfCentralDirectoryOffset >= 0) {
        if (getInt(endOfCentralDirectoryOffset) == 0x06064b50) break
        endOfCentralDirectoryOffset--
    }

    val entriesNumber = getLong(endOfCentralDirectoryOffset + 32)
    val offsetOfCentralDirectory = getLong(endOfCentralDirectoryOffset + 48)

    return Pair(entriesNumber, offsetOfCentralDirectory)
}

private fun LargeDynamicMappedBuffer.Mapping.getUnsignedShort(offset: Int): Int = java.lang.Short.toUnsignedInt(getShort(offset))
