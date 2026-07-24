/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.impl

import org.jetbrains.kotlin.konan.file.fastzip.FastZipArchiveReader
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Shared thread-safe cache for memory-mapped `.klib` archive readers.
 * Reuses opened handles and sliding dynamic buffers across components of the same library.
 */
class FastZipKlibArchiveCache : AutoCloseable {
    private val readers = ConcurrentHashMap<String, FastZipArchiveReader?>()

    fun getOrOpen(file: File): FastZipArchiveReader? {
        val canonicalPath = try { file.canonicalPath } catch (_: Exception) { file.absolutePath }
        return readers.computeIfAbsent(canonicalPath) {
            FastZipArchiveReader.createIfUnmappingPossible(file)
        }
    }

    override fun close() {
        for (reader in readers.values) {
            try {
                reader?.close()
            } catch (_: Exception) {
            }
        }
        readers.clear()
    }
}
