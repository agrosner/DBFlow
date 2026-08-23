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
        assertTrue(
            generatedFiles.any { it.name == "User_Adapter.kt" },
            generatedFiles.joinToString { it.name },
        )
        assertTrue(
            generatedFiles.none { it.name.contains("_Table") },
            "Should not write public *_Table objects\n${generatedFiles.joinToString { it.name }}",
        )
        assertTrue(
            generatedFiles.none { it.readText().contains("GeneratedDatabaseHolderFactory") },
            "Should not write GeneratedDatabaseHolderFactory",
        )
        assertTrue(
            generatedFiles.any { it.readText().contains("registerAdapters") },
            "create() should register adapters",
        )
        val adapter = generatedFiles.first { it.name == "User_Adapter.kt" }.readText()
        assertTrue(
            adapter.contains("user_companionOps"),
            "Adapter file should pre-wire companion ops\n$adapter",
        )
        assertTrue(
            adapter.contains("User_AdapterCompanion"),
            "Internal binders should use file-level adapter companion stand-in\n$adapter",
        )
        assertTrue(
            generatedFiles.none { it.readText().contains("CompanionBase") },
            "Should not generate companion base classes",
        )
        assertTrue(
            generatedFiles.any {
                it.name == "AppDatabase_Database.kt" &&
                    it.readText().contains("User.Companion as")
            },
            "Database create() should use model companion as adapter",
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
            sources = generatedSources + listOf(
                modelsSource(),
                SourceFile.kotlin(
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
            ),
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

    @Test
    fun companionIsVisibleInSameCompilation() {
        val generated = File("build/tmp/compiler-test-companion").apply {
            deleteRecursively()
            mkdirs()
        }
        val result = compile(
            generated = generated,
            sources = listOf(
                modelsSource(),
                SourceFile.kotlin(
                    "CompanionUsage.kt",
                    """
                    package com.example
                    import com.dbflow5.query.select

                    fun query() = select from User
                    """.trimIndent(),
                ),
            ),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, message = result.messages)
    }

    @Test
    fun companionColumnPropertiesCompileWithGeneratedSources() {
        val generated = File("build/tmp/compiler-test-columns").apply {
            deleteRecursively()
            mkdirs()
        }
        val models = compile(generated, listOf(modelsSource()))
        assertEquals(KotlinCompilation.ExitCode.OK, models.exitCode, message = models.messages)

        val generatedSources = generated.walkTopDown()
            .filter { it.extension == "kt" }
            .map { file -> SourceFile.kotlin(file.name, file.readText()) }
            .toList()
        val usage = compile(
            generated = generated,
            sources = generatedSources + listOf(
                modelsSource(),
                SourceFile.kotlin(
                    "ColumnUsage.kt",
                """
                package com.example
                import com.dbflow5.query.select

                fun nameColumn() = User.name
                fun companionSelect() = select from User where (User.name eq "Ada")
                """.trimIndent(),
                ),
            ),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, usage.exitCode, message = usage.messages)
    }

    @Test
    fun companionColumnsAreVisibleFromAnotherPackage() {
        val generated = File("build/tmp/compiler-test-columns-pkg").apply {
            deleteRecursively()
            mkdirs()
        }
        val result = compile(
            generated = generated,
            sources = listOf(
                modelsSource(),
                SourceFile.kotlin(
                    "OtherPkg.kt",
                    """
                    package com.example.usage
                    import com.example.User
                    import com.dbflow5.query.select

                    fun nameColumn() = User.name
                    fun query() = select from User where (User.name eq "Ada")
                    """.trimIndent(),
                ),
            ),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, message = result.messages)
    }

    @Test
    fun companionIsVisibleInLaterCompilationWithPlugin() {
        val generated = File("build/tmp/compiler-test-later-plugin").apply {
            deleteRecursively()
            mkdirs()
        }
        val models = compile(generated, listOf(modelsSource()))
        assertEquals(KotlinCompilation.ExitCode.OK, models.exitCode, message = models.messages)

        val usage = compile(
            generated = generated,
            sources = listOf(
                SourceFile.kotlin(
                    "LaterUsage.kt",
                    """
                    package com.example.later
                    import com.example.User
                    import com.dbflow5.query.select

                    fun query() = select from User
                    fun nameColumn() = User.name
                    """.trimIndent(),
                ),
            ),
            classpaths = listOf(models.outputDirectory),
        )
        assertEquals(KotlinCompilation.ExitCode.OK, usage.exitCode, message = usage.messages)
    }

    @Test
    fun companionPersistsToBinariesWithoutPlugin() {
        val generated = File("build/tmp/compiler-test-persist").apply {
            deleteRecursively()
            mkdirs()
        }
        val models = compile(generated, listOf(modelsSource()))
        assertEquals(KotlinCompilation.ExitCode.OK, models.exitCode, message = models.messages)
        val companionClass = models.outputDirectory.resolve("com/example/User\$Companion.class")
        assertTrue(
            companionClass.exists(),
            "Expected User\$Companion in ${models.outputDirectory.walkTopDown().joinToString()}",
        )

        val usage = compile(
            generated = generated,
            sources = listOf(
                SourceFile.kotlin(
                    "PersistUsage.kt",
                    """
                    package com.example.other
                    import com.example.User
                    import com.dbflow5.query.select

                    fun query() = select from User
                    fun nameColumn() = User.name
                    """.trimIndent(),
                ),
            ),
            classpaths = listOf(models.outputDirectory),
            applyPlugin = false,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, usage.exitCode, message = usage.messages)
    }
}

@OptIn(ExperimentalCompilerApi::class)
private fun compile(
    generated: File,
    sources: List<SourceFile>,
    classpaths: List<File> = emptyList(),
    applyPlugin: Boolean = true,
) = KotlinCompilation().apply {
    this.sources = sources
    inheritClassPath = true
    this.classpaths = classpaths
    jvmTarget = "21"
    if (applyPlugin) {
        compilerPluginRegistrars = listOf(DBFlowCompilerPluginRegistrar())
        commandLineProcessors = listOf(DBFlowCommandLineProcessor())
        pluginOptions = listOf(
            com.tschuchort.compiletesting.PluginOption(
                DBFlowCommandLineProcessor.PLUGIN_ID,
                DBFlowCommandLineProcessor.OPTION_GENERATED_DIR,
                generated.absolutePath,
            )
        )
    }
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
