package com.raizlabs.android.dbflow.processor.utils

import com.grosner.kpoet.end
import com.grosner.kpoet.nextControl
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import kotlin.reflect.KClass

/**
 * Description: Set of utility methods to save code
 *
 * @author Andrew Grosner (fuzz)
 */

/**
 * Collapses the control flow into an easy to use block
 */
fun CodeBlock.Builder.controlFlow(statement: String, vararg args: Any?,
                                  method: (CodeBlock.Builder) -> Unit) = beginControlFlow(statement, *args).apply { method(this) }.endControlFlow()!!

fun MethodSpec.Builder.controlFlow(statement: String, vararg args: Any?,
                                   method: CodeBlock.Builder.() -> Unit) = beginControlFlow(statement, *args).apply {
    addCode(CodeBlock.builder().apply { method(this) }.build())
}.endControlFlow()!!

/**
 * Description: Convenience method for adding [CodeBlock] statements without needing to do so every time.
 *
 * @author Andrew Grosner (fuzz)
 */
fun CodeBlock.Builder.statement(codeBlock: CodeBlock?): CodeBlock.Builder
        = this.addStatement("\$L", codeBlock)

fun MethodSpec.Builder.statement(codeBlock: CodeBlock?): MethodSpec.Builder

        = this.addStatement("\$L", codeBlock)

inline fun <T : Throwable> CodeBlock.Builder.catch(exception: KClass<T>,
                                                   function: CodeBlock.Builder.() -> CodeBlock.Builder)
        = nextControl("catch", statement = "\$T e", args = arrayOf(exception), function = function).end()

fun codeBlock(function: CodeBlock.Builder.() -> CodeBlock.Builder) = CodeBlock.builder().function().build()

