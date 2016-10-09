package com.raizlabs.android.dbflow.processor

import com.squareup.javapoet.CodeBlock

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */

fun CodeBlock.Builder.addStatement(codeBlock: CodeBlock?): CodeBlock.Builder
        = this.addStatement("\$L", codeBlock)