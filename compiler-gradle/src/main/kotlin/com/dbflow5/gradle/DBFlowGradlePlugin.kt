package com.dbflow5.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Metro-style Gradle wiring: [KotlinCompilerPluginSupportPlugin] plus compilation options.
 *
 * Local :compiler classpath and generated-source directories are added by the consumer
 * project so Isolated Projects can declare those edges explicitly.
 */
class DBFlowGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        super.apply(target)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "org.jetbrains.kotlin",
            artifactId = "kotlin-stdlib",
            version = KOTLIN_VERSION,
        )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val generated = project.layout.buildDirectory.dir(GENERATED_DIR)
        return project.provider {
            listOf(
                SubpluginOption(
                    key = "generatedDir",
                    value = generated.get().asFile.absolutePath,
                )
            )
        }
    }

    internal companion object {
        const val PLUGIN_ID = "com.dbflow5.compiler"
        const val GENERATED_DIR = "generated/dbflow/commonMain/kotlin"
        const val KOTLIN_VERSION = "2.4.10"
    }
}
