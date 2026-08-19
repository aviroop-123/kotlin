/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder

/**
 * Checks metadata diagnostics in the test data with reversed resolution order.
 *
 * A counterpart for [AbstractLLMetadataDiagnosticsTest].
 *
 * @see AbstractLLMetadataDiagnosticsTest
 */
abstract class AbstractLLReversedMetadataDiagnosticsTest : AbstractLLReversedDiagnosticsTest() {
    override fun configure(builder: TestConfigurationBuilder) = with(builder) {
        super.configure(builder)
        configureForLLMetadataDiagnosticsTest()
    }
}
