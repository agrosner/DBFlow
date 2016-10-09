package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.utils.lower
import com.squareup.javapoet.CodeBlock

/**
 * Description: Base interface for accessing columns
 *
 * @author Andrew Grosner (fuzz)
 */
interface ColumnAccessor {

    fun get(): CodeBlock

    fun set(existingBlock: CodeBlock): CodeBlock
}

interface GetterSetter {

    val getterName: String
    val setterName: String
}

class VisibleScopeColumnAccessor(val propertyName: String) : ColumnAccessor {

    override fun set(existingBlock: CodeBlock): CodeBlock {
        return CodeBlock.of("\$L = \$L", propertyName, existingBlock)
    }

    override fun get(): CodeBlock {
        return CodeBlock.of(propertyName)
    }
}

class PrivateScopeColumnAccessor : ColumnAccessor {

    private val propertyName: String
    private val useIsForPrivateBooleans: Boolean
    private val isBoolean: Boolean

    private var getterName: String = ""
    private var setterName: String = ""

    constructor(propertyName: String,
                getterSetter: GetterSetter? = null,
                isBoolean: Boolean = false,
                useIsForPrivateBooleans: Boolean = false) {
        this.propertyName = propertyName
        this.isBoolean = isBoolean
        this.useIsForPrivateBooleans = useIsForPrivateBooleans

        getterSetter?.let {
            getterName = getterSetter.getterName
            setterName = getterSetter.setterName
        }
    }

    override fun get(): CodeBlock {
        return CodeBlock.of("\$L()", getGetterNameElement())
    }

    override fun set(existingBlock: CodeBlock): CodeBlock {
        return CodeBlock.of("\$L(\$L)", getSetterNameElement(), existingBlock)
    }

    fun getGetterNameElement(): String {
        return if (getterName.isNullOrEmpty()) {
            if (useIsForPrivateBooleans && !propertyName.startsWith("is")) {
                "is" + propertyName.capitalizeFirstLetter()
            } else if (!useIsForPrivateBooleans && !propertyName.startsWith("get")) {
                "get" + propertyName.capitalizeFirstLetter()
            } else propertyName.lower()
        } else getterName
    }

    fun getSetterNameElement(): String {
        var setElementName = propertyName
        return if (setterName.isNullOrEmpty()) {
            if (!setElementName.startsWith("set")) {
                if (useIsForPrivateBooleans && setElementName.startsWith("is")) {
                    setElementName = setElementName.replaceFirst("is".toRegex(), "")
                }
                "set" + setElementName.capitalizeFirstLetter()
            } else setElementName.lower()
        } else setterName
    }
}



