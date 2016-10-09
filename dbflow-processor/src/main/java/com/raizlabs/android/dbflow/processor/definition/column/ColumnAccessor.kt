package com.raizlabs.android.dbflow.processor.definition.column

import com.google.common.collect.Maps
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.utils.lower
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import java.util.*

/**
 * Description: Base interface for accessing columns
 *
 * @author Andrew Grosner (fuzz)
 */
interface ColumnAccessor {

    fun get(existingBlock: CodeBlock? = null): CodeBlock

    fun set(existingBlock: CodeBlock? = null, baseVariableName: CodeBlock? = null): CodeBlock
}

interface GetterSetter {

    val getterName: String
    val setterName: String
}

class VisibleScopeColumnAccessor(val propertyName: String) : ColumnAccessor {

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L = \$L", propertyName, existingBlock)
    }

    override fun get(existingBlock: CodeBlock?): CodeBlock {
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

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L()", getGetterNameElement())
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
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

class PackagePrivateScopeColumnAccessor(
        val propertyName: String,
        packageName: String, separator: String?, tableClassName: String)
: ColumnAccessor {

    val helperClassName: ClassName
    val internalHelperClassName: ClassName

    init {
        helperClassName = ClassName.get(packageName, "$tableClassName$separator$classSuffix")

        var setSeparator = separator
        if (setSeparator != null && setSeparator.matches("[$]+".toRegex())) {
            setSeparator += setSeparator // duplicate to be safe
        }
        internalHelperClassName = ClassName.get(packageName, "$tableClassName$setSeparator$classSuffix")
    }

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$T.get\$L(\$L)", internalHelperClassName,
                propertyName.capitalizeFirstLetter(),
                existingBlock)
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$T.set\$L(\$L, \$L)", helperClassName,
                propertyName.capitalizeFirstLetter(),
                baseVariableName,
                existingBlock)
    }

    companion object {

        val classSuffix = "Helper"

        private val methodWrittenMap = Maps.newHashMap<ClassName, MutableList<String>>()

        fun containsColumn(className: ClassName, columnName: String): Boolean {
            return methodWrittenMap[className]?.contains(columnName) ?: false
        }

        /**
         * Ensures we only map and use a package private field generated access method if its necessary.
         */
        fun putElement(className: ClassName, elementName: String) {
            var list: MutableList<String>? = methodWrittenMap[className]
            if (list == null) {
                list = ArrayList<String>()
                methodWrittenMap.put(className, list)
            }
            if (!list.contains(elementName)) {
                list.add(elementName)
            }
        }
    }
}

class TypeConverterScopeColumnAccessor(val typeConverterFieldName: String)
: ColumnAccessor {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L.getDBValue(\$L)", typeConverterFieldName,
                existingBlock)
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L.getModelValue(\$L)", typeConverterFieldName,
                existingBlock)
    }

}

class EnumColumnAccessor(val propertyTypeName: TypeName)
: ColumnAccessor {
    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L.name()", existingBlock)
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$T.valueOf(\$L)", propertyTypeName, existingBlock)
    }

}

class BlobColumnAccessor() : ColumnAccessor {
    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L.getBlob()", existingBlock)
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return CodeBlock.of("new \$T(\$L)", ClassName.get(Blob::class.java), existingBlock)
    }

}

class BooleanColumnAccessor() : ColumnAccessor {
    
    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L ? 1 : 0", existingBlock)
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$L == 1 ? true : false", existingBlock)
    }

}

