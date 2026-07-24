/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.library.impl

import org.jetbrains.kotlin.library.KlibLayoutReader
import org.jetbrains.kotlin.library.components.KlibMetadataComponent
import org.jetbrains.kotlin.library.components.KlibMetadataComponentLayout
import org.jetbrains.kotlin.library.components.KlibMetadataConstants.KLIB_METADATA_FILE_EXTENSION_WITH_DOT

/**
 * The default implementation of [KlibMetadataComponent].
 */
internal class KlibMetadataComponentImpl(
    private val layoutReader: KlibLayoutReader<KlibMetadataComponentLayout>,
) : KlibMetadataComponent {

    override val moduleHeaderData get() = layoutReader.readBytes { moduleHeaderFile }

    override fun getPackageFragmentNames(packageFqName: String): Set<String> {
        val fileList: List<String> = layoutReader.listChildNames { getPackageFragmentsDir(packageFqName) }.mapNotNull { name ->
            name.substringBeforeLast(KLIB_METADATA_FILE_EXTENSION_WITH_DOT, missingDelimiterValue = "")
                .takeIf { it.isNotEmpty() }
        }

        return fileList.toSortedSet().also { fileSet ->
            check(fileSet.size == fileList.size) {
                "Duplicated names: ${fileList.groupingBy { it }.eachCount().filter { (_, count) -> count > 1 }}"
            }
        }
    }

    override fun getPackageFragment(packageFqName: String, fragmentName: String): ByteArray =
        layoutReader.readBytes { getPackageFragmentFile(packageFqName, fragmentName) }
}
