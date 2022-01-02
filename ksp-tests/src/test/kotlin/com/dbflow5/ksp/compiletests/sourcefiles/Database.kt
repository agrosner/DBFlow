package com.dbflow5.ksp.compiletests.sourcefiles

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language

/**
 * Description:
 */
@Language("kotlin")
val dbFile = SourceFile.kotlin(
    "Database.kt",
    """
    import com.dbflow5.annotation.Database
    import com.dbflow5.config.DBFlowDatabase

    @Database(version = 1)
    abstract class TestDatabase: DBFlowDatabase()
""".trimIndent()
)