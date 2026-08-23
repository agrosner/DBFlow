package com.dbflow5.gradle

import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

/**
 * Applies the DBFlow compiler plugin and wires everything a consumer used to
 * declare by hand:
 *
 * - the compiler plugin classpath ([getPluginArtifact])
 * - the `com.dbflow5:lib` runtime dependency on `commonMain`/`main`
 * - the generated-sources directory on `commonMain` (KMP) or `main` (single target)
 * - `-lsqlite3` linker opts for Kotlin/Native binaries
 * - a `dbflowGenerate` task plus ordering so generated sources exist before any
 *   Kotlin compilation consumes them
 *
 * Sources are generated at the end of a compilation (IR), so the compilation
 * that produces them can never compile them. [GENERATE_TASK_NAME] therefore
 * runs the Kotlin JVM compiler over the model sources with the DBFlow plugin in
 * generation-only mode, discarding the class output and keeping the generated
 * Kotlin. Every Kotlin compilation runs afterwards and compiles the generated
 * sources as part of `commonMain`/`main`, so main artifacts contain the
 * generated code and test source sets are not involved.
 */
class DBFlowGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        super.apply(target)
        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            target.configureMultiplatform()
        }
        target.plugins.withId("org.jetbrains.kotlin.jvm") {
            target.configureSingleTarget()
        }
        target.plugins.withId("org.jetbrains.kotlin.android") {
            target.configureSingleTarget()
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = GROUP,
            artifactId = COMPILER_ARTIFACT,
            version = VERSION,
        )

    /**
     * Regular compilations never generate sources themselves; the dedicated
     * [GENERATE_TASK_NAME] task passes its own options on the command line.
     */
    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider { emptyList() }

    private fun Project.generatedDir(): Provider<Directory> =
        layout.buildDirectory.dir(GENERATED_DIR)

    private fun Project.configureMultiplatform() {
        val kotlinExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
        val generateTask = registerGenerateTask(
            modelSourceSetName = "commonMain",
            dependencyScopes = listOf(
                "commonMainImplementation",
                "commonMainApi",
                "commonMainCompileOnly",
            ),
        )
        val generated = generatedDir()
        kotlinExtension.sourceSets.named("commonMain") {
            // Mapping through the task provider carries the task dependency to
            // every compilation that consumes the generated sources.
            kotlin.srcDir(generateTask.map { generated.get() })
            dependencies {
                implementation("$GROUP:$LIB_ARTIFACT:$VERSION")
            }
        }
        kotlinExtension.targets.withType(KotlinNativeTarget::class.java).configureEach {
            binaries.all {
                linkerOpts(SQLITE_LINKER_OPT)
            }
        }
    }

    private fun Project.configureSingleTarget() {
        val kotlinExtension = extensions.getByType(KotlinProjectExtension::class.java)
        val generateTask = registerGenerateTask(
            modelSourceSetName = "main",
            dependencyScopes = listOf("implementation", "api", "compileOnly"),
        )
        val generated = generatedDir()
        kotlinExtension.sourceSets.named("main") {
            kotlin.srcDir(generateTask.map { generated.get() })
        }
        dependencies.add("implementation", "$GROUP:$LIB_ARTIFACT:$VERSION")
    }

    /**
     * Runs `K2JVMCompiler` over the model source set with the DBFlow plugin in
     * generation-only mode. The JVM view of the model dependencies provides the
     * compile classpath, which also works for native-only projects as long as
     * their `commonMain` dependencies publish a JVM variant.
     */
    private fun Project.registerGenerateTask(
        modelSourceSetName: String,
        dependencyScopes: List<String>,
    ): org.gradle.api.tasks.TaskProvider<JavaExec> {
        val runnerClasspath = configurations.create("dbflowCompilerRunner") {
            isCanBeConsumed = false
            defaultDependencies {
                add(project.dependencies.create("org.jetbrains.kotlin:kotlin-compiler-embeddable:$KOTLIN_VERSION"))
                add(project.dependencies.create("org.jetbrains.kotlin:kotlin-reflect:$KOTLIN_VERSION"))
                add(project.dependencies.create("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$COROUTINES_VERSION"))
            }
        }
        val compilerPlugin = configurations.create("dbflowCompilerPlugin") {
            isCanBeConsumed = false
            defaultDependencies {
                add(project.dependencies.create("$GROUP:$COMPILER_ARTIFACT:$VERSION"))
            }
        }
        val modelClasspath = configurations.create("dbflowModelClasspath") {
            isCanBeConsumed = false
            attributes {
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
                attribute(
                    Usage.USAGE_ATTRIBUTE,
                    project.objects.named(Usage::class.java, Usage.JAVA_API),
                )
                attribute(
                    Category.CATEGORY_ATTRIBUTE,
                    project.objects.named(Category::class.java, Category.LIBRARY),
                )
            }
        }
        dependencyScopes.forEach { scope ->
            configurations.matching { it.name == scope }.configureEach {
                modelClasspath.extendsFrom(this)
            }
        }

        val generated = generatedDir()
        val discard = layout.buildDirectory.dir("dbflow/generator-classes")

        val generateTask = tasks.register(GENERATE_TASK_NAME, JavaExec::class.java) {
            group = "dbflow"
            description = "Generates DBFlow adapters and database classes from model sources."
            classpath(runnerClasspath)
            mainClass.set("org.jetbrains.kotlin.cli.jvm.K2JVMCompiler")

            // Snapshot configuration-time state into serializable values so the
            // argument provider stays configuration-cache compatible.
            val sourceSet = project.kotlinSourceSet(modelSourceSetName)
            val generatedRoot = generated.get().asFile
            val discardRoot = discard.get().asFile
            val modelSourceDirs = sourceSet?.kotlin?.srcDirs
                ?.filterNot { it.toPath().startsWith(generatedRoot.toPath()) }
                .orEmpty()
            val optIns = sourceSet?.languageSettings?.optInAnnotationsInUse?.toList().orEmpty()
            val modelClasspathFiles = project.objects.fileCollection().from(modelClasspath)
            val compilerPluginFiles = project.objects.fileCollection().from(compilerPlugin)

            inputs.files(modelSourceDirs).withPropertyName("dbflowModelSources")
            inputs.files(modelClasspathFiles).withPropertyName("dbflowModelClasspath")
            inputs.files(compilerPluginFiles).withPropertyName("dbflowCompilerPlugin")
            outputs.dir(generated).withPropertyName("dbflowGeneratedSources")
            outputs.dir(discard).withPropertyName("dbflowGeneratorClasses")
            argumentProviders.add(
                CommandLineArgumentProvider {
                    buildList {
                        add("-no-stdlib")
                        add("-no-reflect")
                        add("-nowarn")
                        add("-Xallow-no-source-files")
                        add("-jvm-target")
                        add(org.gradle.api.JavaVersion.current().majorVersion)
                        add("-classpath")
                        add(modelClasspathFiles.files.joinToString(File.pathSeparator))
                        add("-d")
                        add(discardRoot.absolutePath)
                        add("-Xplugin=${compilerPluginFiles.files.joinToString(",")}")
                        optIns.forEach { optIn ->
                            add("-opt-in=$optIn")
                        }
                        add("-P")
                        add("plugin:$PLUGIN_ID:generatedDir=${generatedRoot.absolutePath}")
                        add("-P")
                        add("plugin:$PLUGIN_ID:mode=generate")
                        modelSourceDirs.filter(File::exists).forEach { dir ->
                            add(dir.absolutePath)
                        }
                    }
                }
            )
        }
        return generateTask
    }

    private fun Project.kotlinSourceSet(name: String): KotlinSourceSet? =
        extensions.findByType(KotlinProjectExtension::class.java)
            ?.sourceSets?.findByName(name)

    internal companion object {
        const val PLUGIN_ID = "com.dbflow5.compiler"
        const val GROUP = "com.dbflow5"
        const val COMPILER_ARTIFACT = "compiler"
        const val LIB_ARTIFACT = "lib"

        /** Keep in sync with the published module versions. */
        const val VERSION = "5.0.0-alpha2"

        /** Keep in sync with the Kotlin version the compiler plugin builds against. */
        const val KOTLIN_VERSION = "2.4.10"
        const val COROUTINES_VERSION = "1.10.2"
        const val GENERATED_DIR = "generated/dbflow/commonMain/kotlin"
        const val SQLITE_LINKER_OPT = "-lsqlite3"
        const val GENERATE_TASK_NAME = "dbflowGenerate"
    }
}
