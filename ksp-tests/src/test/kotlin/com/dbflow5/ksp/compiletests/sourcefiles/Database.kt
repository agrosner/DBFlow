package com.dbflow5.ksp.compiletests.sourcefiles

import org.intellij.lang.annotations.Language

/**
 * Description:
 */
@Language("kotlin")
val dbFile = Source.KotlinSource(
    "test.Database",
    """
    package test
    import com.dbflow5.annotation.Database
    import com.dbflow5.database.DBFlowDatabase

    @Database(version = 1)
    abstract class TestDatabase: DBFlowDatabase()
""".trimIndent()
)