package com.raizlabs.android.dbflow.processor.utils

import com.squareup.javapoet.CodeBlock

/**
 * Description: Set of utility methods to save code
 *
 * @author Andrew Grosner (fuzz)
 */

/**
 * Collapses the control flow into an easy to use block
 */
fun CodeBlock.Builder.controlFlow(statement: String, vararg args: Any?,
                                  method: (CodeBlock.Builder) -> Unit) {
    beginControlFlow(statement, *args)
    method(this)
    endControlFlow()
}
