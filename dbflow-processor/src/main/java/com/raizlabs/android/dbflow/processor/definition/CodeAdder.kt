package com.raizlabs.android.dbflow.processor.definition

import com.squareup.javapoet.CodeBlock

interface CodeAdder {

    fun addCode(code: CodeBlock.Builder)
}
