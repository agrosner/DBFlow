package com.raizlabs.android.dbflow.processor.definition.column

import com.google.common.collect.Maps
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.StringUtils
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName
import java.util.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Description: Wraps the get call in a package-private access class so it can reference the field properly.
 */
class PackagePrivateAccess(elementPackageName: String, separator: String, className: String) : BaseColumnAccess() {

    val helperClassName: ClassName
    private val internalHelperClassName: ClassName // used for safety

    init {
        var separator = separator
        helperClassName = ClassName.get(elementPackageName, className + separator + classSuffix)

        if (separator.matches("[$]+".toRegex())) {
            separator += separator // duplicate to be safe
        }
        internalHelperClassName = ClassName.get(elementPackageName, className + separator + classSuffix)
    }

    override fun getColumnAccessString(fieldType: TypeName, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$T.get\$L(\$L)", internalHelperClassName,
                StringUtils.capitalize(elementName),
                variableNameString).build()
    }

    override fun getShortAccessString(fieldType: TypeName, elementName: String,
                                      isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$T.get\$L(\$L)", internalHelperClassName,
                StringUtils.capitalize(elementName),
                elementName).build()
    }

    override fun setColumnAccessString(fieldType: TypeName, elementName: String,
                                       fullElementName: String,
                                       variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        return CodeBlock.builder().add("\$T.set\$L(\$L, \$L)", helperClassName,
                StringUtils.capitalize(elementName), variableNameString,
                formattedAccess).build()
    }

    companion object {

        val classSuffix = "Helper"

        private val helperUsedMethodMap = Maps.newHashMap<ClassName, MutableList<String>>()

        fun containsColumn(className: ClassName, columnName: String): Boolean {
            val list = helperUsedMethodMap[className]
            if (list == null) {
                return false
            } else {
                return list.contains(columnName)
            }
        }

        /**
         * Ensures we only map and use a package private field generated access method if its necessary.

         * @param className
         * *
         * @param elementName
         */
        fun putElement(className: ClassName, elementName: String) {
            var list: MutableList<String>? = helperUsedMethodMap[className]
            if (list == null) {
                list = ArrayList<String>()
                helperUsedMethodMap.put(className, list)
            }
            if (!list.contains(elementName)) {
                list.add(elementName)
            }
        }

        fun from(processorManager: ProcessorManager, columnElement: Element, classSeparator: String): PackagePrivateAccess {
            return PackagePrivateAccess(processorManager.elements.getPackageOf(columnElement).toString(),
                    classSeparator, ClassName.get(columnElement.enclosingElement as TypeElement).simpleName())
        }
    }
}
