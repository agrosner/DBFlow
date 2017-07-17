package com.raizlabs.android.dbflow.processor.definition

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeSpec

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
interface TypeAdder {

    fun addToType(typeBuilder: TypeSpec.Builder)
}

interface CodeAdder {

    fun addCode(code: CodeBlock.Builder): CodeBlock.Builder
}