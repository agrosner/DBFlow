package com.raizlabs.android.dbflow.processor.definition.column

import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.SQLiteHelper
import com.raizlabs.android.dbflow.processor.definition.TypeConverterDefinition
import com.raizlabs.android.dbflow.processor.model.ProcessorManager
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.TypeName

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */
/**
 * Description: Slightly different access than a regular column and has a type converter.
 */
class BooleanColumnAccess(manager: ProcessorManager, columnDefinition: ColumnDefinition) : TypeConverterAccess(manager, columnDefinition)

/**
 * Description:
 */
class BooleanTypeColumnAccess(columnDefinition: ColumnDefinition) : WrapperColumnAccess(columnDefinition) {

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, isSqliteStatement: Boolean): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        codeBuilder.add(existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement))
        if (isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0")
        }
        return codeBuilder.build()
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        codeBuilder.add(existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement))
        if (isSqliteStatement) {
            codeBuilder.add(" ? 1 : 0")
        }
        return codeBuilder.build()
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        val finalAccess: CodeBlock
        finalAccess = CodeBlock.builder().add("\$L == 1 ? true : false", formattedAccess).build()
        return CodeBlock.builder().add(existingColumnAccess.setColumnAccessString(fieldType,
                elementName, fullElementName, variableNameString, finalAccess)).build()
    }
}

/**
 * Description: Defines how to access a [Blob].
 */
class BlobColumnAccess(columnDefinition: ColumnDefinition) : WrapperColumnAccess(columnDefinition) {

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.getBlob()", existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName,
                variableNameString, isSqliteStatement)).build()
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.getBlob()", existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement)).build()
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       formattedAccess: CodeBlock): CodeBlock {
        val newFormattedAccess = CodeBlock.builder().add("new \$T(\$L)", ClassName.get(Blob::class.java), formattedAccess).build()
        return existingColumnAccess.setColumnAccessString(ArrayTypeName.of(TypeName.BYTE), elementName,
                fullElementName, variableNameString, newFormattedAccess)
    }

    override fun getSqliteTypeForTypeName(elementTypeName: TypeName?): SQLiteHelper {
        return SQLiteHelper.BLOB
    }
}

/**
 * Description: Supports type converters here.
 */
open class TypeConverterAccess : WrapperColumnAccess {

    val typeConverterDefinition: TypeConverterDefinition?

    private val manager: ProcessorManager
    private val typeConverterFieldName: String?

    constructor(manager: ProcessorManager, columnDefinition: ColumnDefinition) : super(columnDefinition) {
        typeConverterDefinition = manager.getTypeConverterDefinition(columnDefinition.elementTypeName?.box())
        this.manager = manager
        this.typeConverterFieldName = null
    }

    constructor(manager: ProcessorManager, columnDefinition: ColumnDefinition,
                typeConverterDefinition: TypeConverterDefinition,
                typeConverterFieldName: String) : super(columnDefinition) {
        this.manager = manager
        this.typeConverterFieldName = typeConverterFieldName
        this.typeConverterDefinition = typeConverterDefinition
    }

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String,
                                       fullElementName: String, variableNameString: String,
                                       isSqliteStatement: Boolean): CodeBlock {
        checkConverter()
        if (typeConverterDefinition != null) {
            val codeBuilder = CodeBlock.builder()
            if (typeConverterFieldName == null) {
                codeBuilder.add("(\$T) \$T.\$L(\$T.class)", typeConverterDefinition.dbTypeName,
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName?.box())
            } else {
                codeBuilder.add(typeConverterFieldName)
            }
            codeBuilder.add(".getDBValue((\$T) \$L)", typeConverterDefinition.modelTypeName,
                    existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement))


            return codeBuilder.build()
        } else {
            return CodeBlock.of("")
        }
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        checkConverter()
        if (typeConverterDefinition != null) {
            val codeBuilder = CodeBlock.builder()
            if (typeConverterFieldName == null) {
                codeBuilder.add("(\$T) \$T.\$L(\$T.class)",
                        typeConverterDefinition.dbTypeName,
                        ClassNames.FLOW_MANAGER,
                        METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName?.box())
            } else {
                codeBuilder.add(typeConverterFieldName)
            }
            codeBuilder.add(".getDBValue(\$L)", existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement))


            return codeBuilder.build()
        } else {
            return CodeBlock.of("")
        }
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        checkConverter()
        if (typeConverterDefinition != null) {
            val newFormattedAccess = CodeBlock.builder()
            if (typeConverterFieldName == null) {
                newFormattedAccess.add("(\$T) \$T.\$L(\$T.class)",
                        typeConverterDefinition.modelTypeName, ClassNames.FLOW_MANAGER, METHOD_TYPE_CONVERTER,
                        columnDefinition.elementTypeName?.box()).build()
            } else {
                newFormattedAccess.add(typeConverterFieldName)
            }

            var newCursorAccess = formattedAccess.toString()
            if (typeConverterDefinition.dbTypeName == ClassName.get(Blob::class.java)) {
                newCursorAccess = CodeBlock.builder().add("new \$T(\$L)", ClassName.get(Blob::class.java),
                        newCursorAccess).build().toString()
            }

            newFormattedAccess.add(".getModelValue(\$L)", newCursorAccess)

            return existingColumnAccess.setColumnAccessString(fieldType, elementName, fullElementName, variableNameString, newFormattedAccess.build())
        } else {
            return CodeBlock.of("")
        }
    }

    override fun getSqliteTypeForTypeName(elementTypeName: TypeName?): SQLiteHelper {
        checkConverter()
        if (typeConverterDefinition != null) {
            return super.getSqliteTypeForTypeName(typeConverterDefinition.dbTypeName)
        } else {
            return SQLiteHelper.TEXT
        }
    }

    private fun checkConverter() {
        if (typeConverterDefinition == null) {
            manager.logError("No type converter for: " + columnDefinition.elementTypeName + " -> " + columnDefinition.elementName + " from class: " + columnDefinition.baseTableDefinition.elementClassName + ". Please" +
                    "register with a TypeConverter.")
        }
    }

    companion object {

        private val METHOD_TYPE_CONVERTER = "getTypeConverterForClass"
    }
}

/**
 * Description:
 */
abstract class WrapperColumnAccess(protected var columnDefinition: ColumnDefinition)
: BaseColumnAccess() {
    var existingColumnAccess: BaseColumnAccess
        protected set

    init {
        this.existingColumnAccess = columnDefinition.columnAccess
    }

}

/**
 * Description:
 */
class EnumColumnAccess(columnDefinition: ColumnDefinition) : WrapperColumnAccess(columnDefinition) {

    override fun getColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.name()", existingColumnAccess.getColumnAccessString(fieldType, elementName, fullElementName, variableNameString, isSqliteStatement)).build()
    }

    override fun getShortAccessString(fieldType: TypeName?, elementName: String, isSqliteStatement: Boolean): CodeBlock {
        return CodeBlock.builder().add("\$L.name()", existingColumnAccess.getShortAccessString(fieldType, elementName, isSqliteStatement)).build()
    }

    override fun setColumnAccessString(fieldType: TypeName?, elementName: String, fullElementName: String, variableNameString: String, formattedAccess: CodeBlock): CodeBlock {
        val newFormattedAccess = CodeBlock.builder().add("\$T.valueOf(\$L)", columnDefinition.elementTypeName, formattedAccess).build()
        return existingColumnAccess.setColumnAccessString(ClassName.get(String::class.java), elementName, fullElementName, variableNameString, newFormattedAccess)
    }
}