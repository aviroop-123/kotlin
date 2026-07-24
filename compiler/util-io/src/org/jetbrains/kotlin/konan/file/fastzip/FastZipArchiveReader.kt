/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.file.fastzip

import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap

class FastZipArchiveEntry internal constructor(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    internal val entryDescription: ZipEntryDescription?
)

/**
 * A fast, VFS-decoupled, memory-mapped reader for KLIB ZIP archives.
 * Supports direct zero-copy byte buffer access for uncompressed entries.
 */
class FastZipArchiveReader(val file: File, internal val unmapBuffer: MappedByteBuffer.() -> Unit) : AutoCloseable {
    private val randomAccessFile: RandomAccessFile
    private val largeBuffer: LargeDynamicMappedBuffer
    private val entriesByPath: Map<String, FastZipArchiveEntry>
    private val childrenByDirectory: Map<String, List<String>>

    init {
        val canonicalFile = file.canonicalFile
        randomAccessFile = RandomAccessFile(canonicalFile, "r")
        largeBuffer = LargeDynamicMappedBuffer(
            randomAccessFile.length(),
            { offset, size -> randomAccessFile.channel.map(FileChannel.MapMode.READ_ONLY, offset, size) },
            unmapBuffer,
            defaultByteOrder = ByteOrder.LITTLE_ENDIAN
        )

        val rawEntries = try {
            largeBuffer.parseCentralDirectory()
        } catch (e: Exception) {
            largeBuffer.unmap()
            randomAccessFile.close()
            throw e
        }

        val entriesMap = HashMap<String, FastZipArchiveEntry>(rawEntries.size + 16)
        val childrenMap = HashMap<String, MutableList<String>>()

        // Root entry
        entriesMap[""] = FastZipArchiveEntry("", isDirectory = true, size = -1, entryDescription = null)

        fun ensureParentDirectories(path: String) {
            var currentPath = path
            while (currentPath.isNotEmpty()) {
                val lastSlash = currentPath.lastIndexOf('/')
                val parentPath = if (lastSlash < 0) "" else currentPath.substring(0, lastSlash)
                val childName = if (lastSlash < 0) currentPath else currentPath.substring(lastSlash + 1)

                val childrenList = childrenMap.getOrPut(parentPath) { mutableListOf() }
                if (childName !in childrenList) {
                    childrenList.add(childName)
                }

                if (parentPath !in entriesMap) {
                    entriesMap[parentPath] = FastZipArchiveEntry(
                        name = if (parentPath.contains('/')) parentPath.substringAfterLast('/') else parentPath,
                        isDirectory = true,
                        size = -1,
                        entryDescription = null
                    )
                }
                currentPath = parentPath
            }
        }

        for (desc in rawEntries) {
            val rawPath = desc.relativePath.toString().removeSuffix("/")
            if (rawPath.isEmpty()) continue

            val shortName = if (rawPath.contains('/')) rawPath.substringAfterLast('/') else rawPath
            val archiveEntry = FastZipArchiveEntry(
                name = shortName,
                isDirectory = desc.isDirectory,
                size = if (desc.isDirectory) -1 else desc.uncompressedSize,
                entryDescription = if (desc.isDirectory) null else desc
            )
            entriesMap[rawPath] = archiveEntry
            ensureParentDirectories(rawPath)
        }

        this.entriesByPath = entriesMap
        this.childrenByDirectory = childrenMap
    }

    private fun normalizePath(path: String): String {
        return path.trim('/').replace('\\', '/')
    }

    fun getEntry(relativePath: String): FastZipArchiveEntry? {
        return entriesByPath[normalizePath(relativePath)]
    }

    fun exists(relativePath: String): Boolean {
        return getEntry(relativePath) != null
    }

    fun isDirectory(relativePath: String): Boolean {
        return getEntry(relativePath)?.isDirectory == true
    }

    fun isFile(relativePath: String): Boolean {
        val entry = getEntry(relativePath)
        return entry != null && !entry.isDirectory
    }

    fun getFileSize(relativePath: String): Long {
        return getEntry(relativePath)?.size ?: -1L
    }

    fun readBytes(relativePath: String): ByteArray {
        val norm = normalizePath(relativePath)
        val entry = entriesByPath[norm] ?: throw FileNotFoundException("Entry not found in KLIB archive $file: $relativePath")
        val desc = entry.entryDescription ?: throw FileNotFoundException("Cannot read directory or empty entry in KLIB archive $file: $relativePath")
        synchronized(largeBuffer) {
            return largeBuffer.contentsToByteArray(desc)
        }
    }

    /**
     * Fast-path zero-copy reading for uncompressed (STORED) entries in a KLIB.
     * Invokes [block] with a direct memory-mapped [ByteBuffer] slice covering the uncompressed entry.
     * Returns null if entry is missing, compressed, or a directory.
     */
    fun <R> withDirectBuffer(relativePath: String, block: (ByteBuffer) -> R): R? {
        val norm = normalizePath(relativePath)
        val entry = entriesByPath[norm] ?: return null
        val desc = entry.entryDescription ?: return null
        synchronized(largeBuffer) {
            return largeBuffer.withDirectBuffer(desc, block)
        }
    }

    fun listChildren(relativePath: String): List<String> {
        val norm = normalizePath(relativePath)
        return childrenByDirectory[norm] ?: emptyList()
    }

    override fun close() {
        synchronized(largeBuffer) {
            largeBuffer.unmap()
            try {
                randomAccessFile.close()
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        fun prepareCleanerCallback(): ((ByteBuffer) -> Unit)? {
            return try {
                val IS_PRIOR_9_JRE = System.getProperty("java.specification.version", "").startsWith("1.")
                if (IS_PRIOR_9_JRE) {
                    val cleaner = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner")
                    cleaner.isAccessible = true
                    val clean = Class.forName("sun.misc.Cleaner").getMethod("clean")
                    clean.isAccessible = true
                    val callback: (ByteBuffer) -> Unit = { buffer -> cleaner.invoke(buffer)?.let { clean.invoke(it) } }
                    callback
                } else {
                    val unsafeClass = try {
                        Class.forName("sun.misc.Unsafe")
                    } catch (_: Exception) {
                        Class.forName("jdk.internal.misc.Unsafe")
                    }
                    val clean = unsafeClass.getMethod("invokeCleaner", ByteBuffer::class.java)
                    clean.isAccessible = true
                    val theUnsafeField = unsafeClass.getDeclaredField("theUnsafe")
                    theUnsafeField.isAccessible = true
                    val theUnsafe = theUnsafeField.get(null)
                    val callback: (ByteBuffer) -> Unit = { buffer -> clean.invoke(theUnsafe, buffer) }
                    callback
                }
            } catch (_: Exception) {
                null
            }
        }

        fun createIfUnmappingPossible(file: File): FastZipArchiveReader? {
            val cleanerCallback = prepareCleanerCallback() ?: return null
            return try {
                FastZipArchiveReader(file, cleanerCallback)
            } catch (_: Exception) {
                null
            }
        }
    }
}
