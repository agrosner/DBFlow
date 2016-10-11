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
abstract class ColumnAccessor(val propertyName: String?) {

    abstract fun get(existingBlock: CodeBlock? = null): CodeBlock

    abstract fun set(existingBlock: CodeBlock? = null, baseVariableName: CodeBlock? = null): CodeBlock

    protected fun prependPropertyName(code: CodeBlock.Builder) {
        propertyName?.let {
            code.add("\$L.", propertyName)
        }
    }

    protected fun appendPropertyName(code: CodeBlock.Builder) {
        propertyName?.let {
            code.add(".\$L", propertyName)
        }
    }

    protected fun appendAccess(codeAccess: CodeBlock.Builder.() -> Unit): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        prependPropertyName(codeBuilder)
        codeAccess(codeBuilder)
        return codeBuilder.build()
    }
}

interface GetterSetter {

    val getterName: String
    val setterName: String
}

class VisibleScopeColumnAccessor(propertyName: String) : ColumnAccessor(propertyName) {

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        val codeBlock: CodeBlock.Builder = CodeBlock.builder()
        baseVariableName?.let { codeBlock.add("\$L.", baseVariableName) }
        return codeBlock.add("\$L = \$L", propertyName, existingBlock)
                .build()
    }

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        val codeBlock: CodeBlock.Builder = CodeBlock.builder()
        existingBlock?.let { codeBlock.add("\$L.", existingBlock) }
        return codeBlock.add(propertyName)
                .build()
    }
}

class PrivateScopeColumnAccessor : ColumnAccessor {

    private val useIsForPrivateBooleans: Boolean
    private val isBoolean: Boolean

    private var getterName: String = ""
    private var setterName: String = ""

    constructor(propertyName: String,
                getterSetter: GetterSetter? = null,
                isBoolean: Boolean = false,
                useIsForPrivateBooleans: Boolean = false) : super(propertyName) {
        this.isBoolean = isBoolean
        this.useIsForPrivateBooleans = useIsForPrivateBooleans

        getterSetter?.let {
            getterName = getterSetter.getterName
            setterName = getterSetter.setterName
        }
    }

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        val codeBlock: CodeBlock.Builder = CodeBlock.builder()
        existingBlock?.let { codeBlock.add("\$L.", existingBlock) }
        return codeBlock.add("\$L()", getGetterNameElement())
                .build()
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        val codeBlock: CodeBlock.Builder = CodeBlock.builder()
        baseVariableName?.let { codeBlock.add("\$L.", baseVariableName) }
        return codeBlock.add("\$L(\$L)", getSetterNameElement(), existingBlock)
                .build()
    }

    fun getGetterNameElement(): String {
        return if (getterName.isNullOrEmpty()) {
            if (propertyName != null) {
                if (useIsForPrivateBooleans && !propertyName.startsWith("is")) {
                    "is" + propertyName.capitalizeFirstLetter()
                } else if (!useIsForPrivateBooleans && !propertyName.startsWith("get")) {
                    "get" + propertyName.capitalizeFirstLetter()
                } else propertyName.lower()
            } else {
                ""
            }
        } else getterName
    }

    fun getSetterNameElement(): String {
        if (propertyName != null) {
            var setElementName = propertyName
            return if (setterName.isNullOrEmpty()) {
                if (!setElementName.startsWith("set")) {
                    if (useIsForPrivateBooleans && setElementName.startsWith("is")) {
                        setElementName = setElementName.replaceFirst("is".toRegex(), "")
                    }
                    "set" + setElementName.capitalizeFirstLetter()
                } else setElementName.lower()
            } else setterName
        } else return ""
    }
}

class PackagePrivateScopeColumnAccessor(
        propertyName: String, packageName: String, separator: String?, tableClassName: String)
: ColumnAccessor(propertyName) {

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

class TypeConverterScopeColumnAccessor(val typeConverterFieldName: String,
                                       propertyName: String? = null)
: ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        val codeBlock = CodeBlock.builder()
        codeBlock.add("\$L.getDBValue(\$L", typeConverterFieldName, existingBlock)
        appendPropertyName(codeBlock)
        codeBlock.add(")")
        return codeBlock.build()
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        val codeBlock = CodeBlock.builder()
        codeBlock.add("\$L.getModelValue(\$L", typeConverterFieldName, existingBlock)
        appendPropertyName(codeBlock)
        codeBlock.add(")")
        return codeBlock.build()
    }

}

class EnumColumnAccessor(val propertyTypeName: TypeName,
                         propertyName: String? = null)
: ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L.name()", existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return appendAccess { add("\$T.valueOf(\$L)", propertyTypeName, existingBlock) }
    }

}

class BlobColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L.getBlob()", existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return appendAccess { add("new \$T(\$L)", ClassName.get(Blob::class.java), existingBlock) }
    }

}

class BooleanColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L ? 1 : 0", existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L == 1 ? true : false", existingBlock) }
    }

}

class CharColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("new \$T(new char[]{\$L})", TypeName.get(String::class.java), existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L.charAt(0)", existingBlock) }
    }

}