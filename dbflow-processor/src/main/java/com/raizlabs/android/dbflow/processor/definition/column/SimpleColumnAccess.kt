package com.raizlabs.android.dbflow.processor.definition.column

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description: Simplest of all fields. Simply returns "model.field".
 */
class SimpleColumnAccess @JvmOverloads constructor(private val dontAppendModel: Boolean = false) : BaseColumnAccess() {

    override fun getColumnAccessString(fieldType: TypeName, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.of(if (dontAppendModel) elementName else variableNameString + "." + fullElementName)
    }

    override fun getShortAccessString(fieldType: TypeName, elementName: String,
                                      isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.of(elementName)
    }

    override fun setColumnAccessString(fieldType: TypeName, elementName: String,
                                       fullElementName: String,
                                       variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        return CodeBlock.of("\$L = \$L", getColumnAccessString(fieldType, elementName, fullElementName,
                variableNameString, false), formattedAccess)
    }
}
