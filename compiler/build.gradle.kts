import org.gradle.api.tasks.bundling.Jar

plugins {
    id("org.jetbrains.kotlin.jvm")
}

group = "com.dbflow5"
version = "5.0.0-alpha2"

configureJdk(
    "org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
    "org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI",
    "kotlin.ExperimentalStdlibApi",
)

/**
 * Kotlin/Native loads compiler plugins in an isolated classloader that does not
 * see Gradle transitive dependencies (KT-53477). Metro embeds those deps in a
 * shadow JAR; we do the same with the default jar so Native compilations can
 * load codegen types such as [com.dbflow5.codegen.shared.validation.ValidationException].
 */
val pluginEmbedded = configurations.dependencyScope("pluginEmbedded")
val pluginEmbeddedClasspath = configurations.resolvable("pluginEmbeddedClasspath") {
    extendsFrom(pluginEmbedded.get())
}

configurations.named("compileOnly").configure { extendsFrom(pluginEmbedded.get()) }
configurations.named("testImplementation").configure { extendsFrom(pluginEmbedded.get()) }

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    add("pluginEmbedded", project(":core"))
    add("pluginEmbedded", project(":shared-model"))
    add("pluginEmbedded", project(":kotlin-codegen"))
    add("pluginEmbedded", libs.koin)
    add("pluginEmbedded", libs.kotlinpoet)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinCompileTesting)
    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(project(":lib"))
    testImplementation(project(":core"))
}

tasks.named<Jar>("jar") {
    val embeddedFiles = pluginEmbeddedClasspath
    dependsOn(embeddedFiles)
    from({
        embeddedFiles.get()
            .filter { file -> file.shouldEmbedInCompilerPlugin() }
            .map { file -> if (file.isDirectory) file else zipTree(file) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("module-info.class")
    exclude("META-INF/versions/*/module-info.class")
}

private fun java.io.File.shouldEmbedInCompilerPlugin(): Boolean {
    val name = name.lowercase()
    return !name.startsWith("kotlin-stdlib") &&
        !name.startsWith("kotlin-reflect") &&
        !name.startsWith("kotlin-compiler") &&
        !name.startsWith("kotlin-gradle")
}
