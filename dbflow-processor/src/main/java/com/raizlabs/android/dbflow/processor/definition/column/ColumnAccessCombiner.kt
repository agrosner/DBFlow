package com.raizlabs.android.dbflow.processor.definition.column

import com.squareup.javapoet.CodeBlock

abstract class ColumnAccessCombiner(val fieldLevelAccessor: ColumnAccessor,
                                    val wrapperLevelAccessor: ColumnAccessor? = null) {

    abstract fun addCode(code: CodeBlock.Builder, columnName: String, defaultValue: CodeBlock)
}

class ContentValuesCombiner(fieldLevelAccessor: ColumnAccessor,
                            wrapperLevelAccessor: ColumnAccessor? = null)
: ColumnAccessCombiner(fieldLevelAccessor, wrapperLevelAccessor) {

    override fun addCode(code: CodeBlock.Builder, columnName: String,
                         defaultValue: CodeBlock) {
        val fieldAccess: CodeBlock
        if (wrapperLevelAccessor != null) {
            fieldAccess = CodeBlock.of("ref" + fieldLevelAccessor.propertyName)

            code.add("\$L = \$L != null ? \$L : null", fieldAccess,
                    fieldLevelAccessor.get(CodeBlock.of("model")),
                    wrapperLevelAccessor.get(fieldLevelAccessor.get(CodeBlock.of("model"))))
        } else {
            fieldAccess = fieldLevelAccessor.get(CodeBlock.of("model"))
        }

        code.add("values.put(\$1S, \$2L != null ? \$2L : \$3L)", columnName, fieldAccess, defaultValue)
    }


}