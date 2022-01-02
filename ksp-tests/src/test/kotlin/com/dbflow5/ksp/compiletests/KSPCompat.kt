package com.dbflow5.ksp.compiletests

import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File

internal val KotlinCompilation.Result.workingDir: File
    get() =
        outputDirectory.parentFile!!

/**
 * Patches issue where KSP is not detected by compile testing by retrieving the
 * actual source files.
 */
val KotlinCompilation.Result.kspGeneratedSources: List<File>
    get() {
        val kspWorkingDir = workingDir.resolve("ksp")
        val kspGeneratedDir = kspWorkingDir.resolve("sources")
        val kotlinGeneratedDir = kspGeneratedDir.resolve("kotlin")
        val javaGeneratedDir = kspGeneratedDir.resolve("java")
        return (kotlinGeneratedDir.walkTopDown().toList() +
            javaGeneratedDir.walkTopDown()).filterNot { it.isDirectory }
    }
