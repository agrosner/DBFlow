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
        val result = KotlinCompilation().apply {
            sources = listOf(
                SourceFile.kotlin(
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
            )
            inheritClassPath = true
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

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode, result.messages)
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
}
