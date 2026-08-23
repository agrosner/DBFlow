package com.dbflow5.ksp.compiletests.sourcefiles

import org.intellij.lang.annotations.Language

/**
 * Description:
 */
@Language("kotlin")
val simpleModelFile = Source.KotlinSource(
    "test.SimpleModel",
    """
    package test
    import com.dbflow5.annotation.PrimaryKey
    import com.dbflow5.annotation.Table

    @Table(database = TestDatabase::class)
    data class SimpleModel(@PrimaryKey val name: String)
""".trimIndent()
)