/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostic.compiler.based

import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.METADATA_ONLY_COMPILATION
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.LANGUAGE
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.MetadataEnvironmentConfigurator

/**
 * Checks metadata diagnostics in the test data with regular resolution order.
 *
 * A counterpart for [AbstractLLReversedMetadataDiagnosticsTest].
 *
 * @see AbstractLLReversedMetadataDiagnosticsTest
 */
abstract class AbstractLLMetadataDiagnosticsTest : AbstractLLDiagnosticsTest() {
    override fun configure(builder: TestConfigurationBuilder) = with(builder) {
        super.configure(builder)
        configureForLLMetadataDiagnosticsTest()
    }
}

fun TestConfigurationBuilder.configureForLLMetadataDiagnosticsTest() {
    defaultDirectives {
        +WITH_STDLIB
        +METADATA_ONLY_COMPILATION
        LANGUAGE with "+MultiPlatformProjects"
    }

    globalDefaults {
        targetPlatform = CommonPlatforms.defaultCommonPlatform
    }

    // Environment configurators are used to set up `KaModule`s from the compiler configuration.
    useConfigurators(
        ::CommonEnvironmentConfigurator,
        ::MetadataEnvironmentConfigurator,
    )
}
