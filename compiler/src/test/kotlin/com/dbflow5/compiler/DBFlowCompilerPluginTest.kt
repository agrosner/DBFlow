package com.dbflow5.compiler

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCompilerApi::class)
class DBFlowCompilerPluginTest {

    @Test
    fun generatesDatabaseHolderFromAnnotatedTypes() {
        val generated = File("build/tmp/compiler-test-generated").apply {
            deleteRecursively()
            mkdirs()
        }
        System.setProperty(com.dbflow5.compiler.ir.GENERATED_DIR_PROPERTY, generated.absolutePath)
        val result = compile(generated, listOf(modelsSource()))

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, message = result.messages)
        val generatedFiles = generated.walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue(
            generatedFiles.isNotEmpty(),
            "Expected generated Kotlin sources in $generated\n${result.messages}",
        )
        assertTrue(
            generatedFiles.any { it.readText().contains("AppDatabase_Database") },
            generatedFiles.joinToString { it.name },
        )
    }

    @Test
    fun rewritesCreateDbToGeneratedFactory() {
        val generated = File("build/tmp/compiler-test-createdb").apply {
            deleteRecursively()
            mkdirs()
        }
        val models = compile(
            generated = generated,
            sources = listOf(modelsSource()),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, models.exitCode, message = models.messages)

        val generatedSources = generated.walkTopDown()
            .filter { it.extension == "kt" }
            .map { file ->
                SourceFile.kotlin(
                    file.name,
                    file.readText(),
                )
            }
            .toList()
        assertTrue(generatedSources.isNotEmpty(), models.messages)

        val usage = compile(
            generated = generated,
            sources = generatedSources + SourceFile.kotlin(
                "Usage.kt",
                """
                package com.example
                import com.dbflow5.database.config.DBPlatformSettings
                import com.dbflow5.database.createDB

                fun open(): AppDatabase = createDB<AppDatabase>(DBPlatformSettings()) {
                    copy(name = "App", inMemory = true)
                }

                fun openDefault(): AppDatabase = createDB<AppDatabase> {
                    copy(name = "Default", inMemory = true)
                }
                """.trimIndent(),
            ),
            classpaths = listOf(models.outputDirectory),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, usage.exitCode, message = usage.messages)
        val classFile = usage.outputDirectory.resolve("com/example/UsageKt.class")
        assertTrue(classFile.exists(), usage.outputDirectory.walkTopDown().joinToString())
        val bytecode = classFile.readText(Charsets.ISO_8859_1)
        assertTrue(
            bytecode.contains("AppDatabase_Database"),
            "createDB should invoke AppDatabase_Database.create\n${bytecode.take(400)}",
        )
    }
}

@OptIn(ExperimentalCompilerApi::class)
private fun compile(
    generated: File,
    sources: List<SourceFile>,
    classpaths: List<File> = emptyList(),
) = KotlinCompilation().apply {
    this.sources = sources
    inheritClassPath = true
    this.classpaths = classpaths
    jvmTarget = "21"
    compilerPluginRegistrars = listOf(DBFlowCompilerPluginRegistrar())
    commandLineProcessors = listOf(DBFlowCommandLineProcessor())
    pluginOptions = listOf(
        com.tschuchort.compiletesting.PluginOption(
            DBFlowCommandLineProcessor.PLUGIN_ID,
            DBFlowCommandLineProcessor.OPTION_GENERATED_DIR,
            generated.absolutePath,
        )
    )
}.compile()

private fun modelsSource(): SourceFile = SourceFile.kotlin(
    "Models.kt",
    """
    package com.example
    import com.dbflow5.annotation.Column
    import com.dbflow5.annotation.Database
    import com.dbflow5.annotation.PrimaryKey
    import com.dbflow5.annotation.Table
    import com.dbflow5.database.DBFlowDatabase

    @Table(database = AppDatabase::class)
    class User(
        @PrimaryKey var id: Int = 0,
        @Column var name: String? = null,
    )

    @Database(version = 1, tables = [User::class])
    abstract class AppDatabase : DBFlowDatabase<AppDatabase>()
    """.trimIndent(),
)
