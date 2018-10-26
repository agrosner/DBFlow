package com.dbflow5.processor.definition.column

import com.dbflow5.data.Blob
import com.dbflow5.processor.utils.capitalizeFirstLetter
import com.dbflow5.processor.utils.isNullOrEmpty
import com.dbflow5.processor.utils.lower
import com.grosner.kpoet.code
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

val modelBlock: CodeBlock = CodeBlock.of("model")

/**
 * Description: Base interface for accessing columns
 *
 * @author Andrew Grosner (fuzz)
 */
abstract class ColumnAccessor(val propertyName: String?) {

    open val isPrimitiveTarget: Boolean = false

    abstract fun get(existingBlock: CodeBlock? = null): CodeBlock

    abstract fun set(existingBlock: CodeBlock? = null, baseVariableName: CodeBlock? = null,
                     isDefault: Boolean = false): CodeBlock

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

fun ColumnAccessor?.isPrimitiveTarget(): Boolean = this?.isPrimitiveTarget ?: true

interface GetterSetter {

    val getterName: String
    val setterName: String
}

class VisibleScopeColumnAccessor(propertyName: String) : ColumnAccessor(propertyName) {

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
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

class PrivateScopeColumnAccessor(propertyName: String, getterSetter: GetterSetter? = null,
                                 private val useIsForPrivateBooleans: Boolean = false,
                                 private val optionalGetterParam: String? = "")
    : ColumnAccessor(propertyName) {

    private var getterName: String = ""
    private var setterName: String = ""

    override fun get(existingBlock: CodeBlock?) = code {
        existingBlock?.let { this.add("$existingBlock.") }
        add("$getterNameElement($optionalGetterParam)")
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean) = code {
        baseVariableName?.let { add("$baseVariableName.") }
        add("$setterNameElement($existingBlock)")
    }

    val getterNameElement: String
        get() = if (getterName.isNullOrEmpty()) {
            if (propertyName != null) {
                if (useIsForPrivateBooleans && !propertyName.startsWith("is", ignoreCase = true)) {
                    "is${propertyName.capitalize()}"
                } else if (!useIsForPrivateBooleans) {
                    "get${propertyName.capitalize()}"
                } else propertyName.lower()
            } else {
                ""
            }
        } else getterName

    val setterNameElement: String
        get() = if (propertyName != null) {
            var setElementName = propertyName
            if (setterName.isNullOrEmpty()) {
                if (!setElementName.startsWith("set", ignoreCase = true)) {
                    if (useIsForPrivateBooleans && setElementName.startsWith("is")) {
                        setElementName = setElementName.replaceFirst("is".toRegex(), "")
                    } else if (useIsForPrivateBooleans && setElementName.startsWith("Is")) {
                        setElementName = setElementName.replaceFirst("Is".toRegex(), "")
                    }
                    "set${setElementName.capitalize()}"
                } else "set${setElementName.capitalize()}"
            } else setterName
        } else ""

    init {
        getterSetter?.let {
            getterName = getterSetter.getterName
            setterName = getterSetter.setterName
        }
    }
}

class PackagePrivateScopeColumnAccessor(
        propertyName: String, packageName: String, separator: String?, tableClassName: String)
    : ColumnAccessor(propertyName) {

    val helperClassName: ClassName
    val internalHelperClassName: ClassName

    init {
        helperClassName = ClassName.get(packageName, "$tableClassName$separator$classSuffix")
        internalHelperClassName = ClassName.get(packageName, "$tableClassName$separator$classSuffix")
    }

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return CodeBlock.of("\$T.get\$L(\$L)", internalHelperClassName,
                propertyName.capitalizeFirstLetter(),
                existingBlock)
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
        return CodeBlock.of("\$T.set\$L(\$L, \$L)", helperClassName,
                propertyName.capitalizeFirstLetter(),
                baseVariableName,
                existingBlock)
    }

    companion object {

        val classSuffix = "Helper"

        private val methodWrittenMap = hashMapOf<ClassName, MutableList<String>>()

        fun containsColumn(className: ClassName, columnName: String): Boolean {
            return methodWrittenMap[className]?.contains(columnName) ?: false
        }

        /**
         * Ensures we only map and use a package private field generated access method if its necessary.
         */
        fun putElement(className: ClassName, elementName: String) {
            val list = methodWrittenMap.getOrPut(className) { arrayListOf() }
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

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
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

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
        return appendAccess {
            if (isDefault) add(existingBlock)
            else {
                add("\$T.valueOf(\$L)", propertyTypeName, existingBlock)
            }
        }
    }

}

class BlobColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L.getBlob()", existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
        return appendAccess {
            if (isDefault) add(existingBlock)
            else add("new \$T(\$L)", ClassName.get(Blob::class.java), existingBlock)
        }
    }

}

class BooleanColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L ? 1 : 0", existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
        return appendAccess {
            if (isDefault) add(existingBlock)
            else add("\$L", existingBlock)
        }
    }

    override val isPrimitiveTarget: Boolean = true
}

class CharColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {

    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("new \$T(new char[]{\$L})", TypeName.get(String::class.java), existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
        return appendAccess {
            if (isDefault) add(existingBlock)
            else add("\$L.charAt(0)", existingBlock)
        }
    }

    override val isPrimitiveTarget: Boolean = true

}

class ByteColumnAccessor(propertyName: String? = null) : ColumnAccessor(propertyName) {
    override fun get(existingBlock: CodeBlock?): CodeBlock {
        return appendAccess { add("\$L", existingBlock) }
    }

    override fun set(existingBlock: CodeBlock?, baseVariableName: CodeBlock?,
                     isDefault: Boolean): CodeBlock {
        return appendAccess {
            if (isDefault) add(existingBlock)
            else add("(\$T) \$L", TypeName.BYTE, existingBlock)
        }
    }

    override val isPrimitiveTarget: Boolean = true
}