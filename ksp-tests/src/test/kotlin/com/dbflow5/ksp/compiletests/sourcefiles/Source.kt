package com.dbflow5.ksp.compiletests.sourcefiles

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import java.io.File

/**
 * Description: workaround used in
 * [https://github.com/airbnb/DeepLinkDispatch/blob/master/deeplinkdispatch-processor/src/test/java/com/airbnb/deeplinkdispatch/test/Source.kt]
 * to solve
 * [https://github.com/tschuchortdev/kotlin-compile-testing/issues/105]
 *
 * to ensure java / kotlin files are resolved.
 */
sealed class Source {
    abstract val contents: String
    abstract fun toKotlinSourceFile(srcRoot: File): SourceFile

    class JavaSource(private val qName: String, override val contents: String) : Source() {
        override fun toKotlinSourceFile(srcRoot: File): SourceFile {
            val outFile = srcRoot.resolve(qName.replace(".", "/") + ".java")
                .also {
                    it.parentFile?.mkdirs()
                    it.writeText(contents.trimIndent())
                }
            return SourceFile.fromPath(outFile)
        }
    }

    class KotlinSource(
        private val relativePath: String,
        @Language("kotlin")
        override val contents: String
    ) : Source() {

        override fun toKotlinSourceFile(srcRoot: File): SourceFile {
            val outFile = srcRoot.resolve("$relativePath.kt").also {
                it.parentFile?.mkdirs()
                it.writeText(contents.trimIndent())
            }
            return SourceFile.fromPath(outFile)
        }
    }
}
