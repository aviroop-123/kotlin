plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    val coreDepsVersion = libs.versions.kotlin.`for`.gradle.plugins.compilation.get()
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$coreDepsVersion")
    testImplementation(libs.junit4)
    testImplementation(kotlin("test"))
    implementation("com.github.luben:zstd-jni:1.5.7-4")
    implementation("org.apache.commons:commons-compress:1.28.0")
}

sourceSets {
    "main" { projectDefault() }
    "test" { projectDefault() }
}

configureKotlinCompileTasksGradleCompatibility()

publish()

standardPublicJars()
