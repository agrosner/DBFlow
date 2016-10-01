package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.utils.lower
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description:
 */
class PrivateColumnAccess : BaseColumnAccess {

    private var getterName: String = ""
    private var setterName: String = ""
    val useBooleanSetters: Boolean

    constructor(column: Column?, useBooleanSetters: Boolean) {
        if (column != null) {
            getterName = column.getterName
            setterName = column.setterName
        }
        this.useBooleanSetters = useBooleanSetters
    }

    constructor(reference: ForeignKeyReference) {
        getterName = reference.referencedGetterName
        setterName = reference.referencedSetterName
        this.useBooleanSetters = false
    }

    constructor(useBooleanSetters: Boolean) {
        this.useBooleanSetters = useBooleanSetters
    }

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.of("\$L.\$L()", variableNameString,
                getGetterNameElement(elementName))
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String,
                                      isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.of("\$L()", getGetterNameElement(elementName))
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String,
                                       variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        // append . when specify something, if not then we leave blank.
        var varNameFull = variableNameString
        if (!varNameFull.isNullOrEmpty()) {
            varNameFull += "."
        }
        return CodeBlock.of("\$L\$L(\$L)", varNameFull, getSetterNameElement(elementName),
                formattedAccess)
    }

    fun getGetterNameElement(elementName: String): String {
        if (getterName.isNullOrEmpty()) {
            if (useBooleanSetters && !elementName.startsWith("is")) {
                return "is" + elementName.capitalizeFirstLetter()
            } else if (!useBooleanSetters && !elementName.startsWith("get")) {
                return "get" + elementName.capitalizeFirstLetter()
            } else {
                return elementName.lower()
            }
        } else {
            return getterName
        }
    }

    fun getSetterNameElement(elementName: String): String {
        var setElementName = elementName
        if (setterName.isNullOrEmpty()) {
            if (!setElementName.startsWith("set")) {
                if (useBooleanSetters && setElementName.startsWith("is")) {
                    setElementName = setElementName.replaceFirst("is".toRegex(), "")
                }
                return "set" + setElementName.capitalizeFirstLetter()
            } else {
                return setElementName.lower()
            }
        } else {
            return setterName
        }
    }
}
