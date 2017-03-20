package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ManyToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.*
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

/**
 * Description: Generates the Model class that is used in a many to many.
 */
class ManyToManyDefinition @JvmOverloads constructor(element: TypeElement, processorManager: ProcessorManager,
                                                     manyToMany: ManyToMany = element.getAnnotation(ManyToMany::class.java))
    : BaseDefinition(element, processorManager) {

    internal var referencedTable: TypeName
    var databaseTypeName: TypeName? = null
    internal var generateAutoIncrement: Boolean = false
    internal var sameTableReferenced: Boolean = false
    internal val generatedTableClassName = manyToMany.generatedTableClassName
    internal var saveForeignKeyModels: Boolean = false
    internal val thisColumnName = manyToMany.thisTableColumnName
    internal val referencedColumnName = manyToMany.referencedTableColumnName

    init {

        var clazz: TypeMirror? = null
        try {
            manyToMany.referencedTable
        } catch (mte: MirroredTypeException) {
            clazz = mte.typeMirror
        }
        referencedTable = TypeName.get(clazz)
        generateAutoIncrement = manyToMany.generateAutoIncrement
        saveForeignKeyModels = manyToMany.saveForeignKeyModels

        sameTableReferenced = referencedTable == elementTypeName

        val table = element.getAnnotation(Table::class.java)
        try {
            table.database
        } catch (mte: MirroredTypeException) {
            databaseTypeName = TypeName.get(mte.typeMirror)
        }

        if (!thisColumnName.isNullOrEmpty() && !referencedColumnName.isNullOrEmpty()
                && thisColumnName == referencedColumnName) {
            manager.logError(ManyToManyDefinition::class, "The thisTableColumnName and referenceTableColumnName" + "cannot be the same")
        }
    }

    fun prepareForWrite() {
        val databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName)?.databaseDefinition
        if (databaseDefinition == null) {
            manager.logError("DatabaseDefinition was null for : " + elementName)
        } else {
            if (generatedTableClassName.isNullOrEmpty()) {
                val referencedOutput = getElementClassName(referencedTable.toTypeElement(manager))
                setOutputClassName(databaseDefinition.classSeparator + referencedOutput?.simpleName())
            } else {
                setOutputClassNameFull(generatedTableClassName)
            }
        }
    }

    override fun onWriteDefinition(typeBuilder: TypeSpec.Builder) {
        typeBuilder.addAnnotation(AnnotationSpec.builder(Table::class.java)
                .addMember("database", "\$T.class", databaseTypeName).build())

        val referencedDefinition = manager.getTableDefinition(databaseTypeName, referencedTable)
        val selfDefinition = manager.getTableDefinition(databaseTypeName, elementTypeName)

        if (generateAutoIncrement) {
            typeBuilder.apply {
                field(TypeName.LONG name "_id") {
                    annotation(PrimaryKey::class, "autoincrement", "true")
                }

                method("getId" returns TypeName.LONG modifiers listOf(Modifier.PUBLIC, Modifier.FINAL)) {
                    addStatement("return \$L", "_id")
                }
            }
        }

        referencedDefinition?.let { appendColumnDefinitions(typeBuilder, it, 0, referencedColumnName) }
        selfDefinition?.let { appendColumnDefinitions(typeBuilder, it, 1, thisColumnName) }
    }

    override val extendsClass: TypeName?
        get() = ClassNames.BASE_MODEL

    private fun appendColumnDefinitions(typeBuilder: TypeSpec.Builder,
                                        referencedDefinition: TableDefinition, index: Int, optionalName: String) {
        var fieldName = referencedDefinition.elementName.lower()
        if (sameTableReferenced) {
            fieldName += index.toString()
        }
        // override with the name (if specified)
        if (!optionalName.isNullOrEmpty()) {
            fieldName = optionalName
        }

        typeBuilder.apply {
            field(referencedDefinition.elementClassName name fieldName) {
                if (!generateAutoIncrement) {
                    annotation(PrimaryKey::class)
                }
                annotation(ForeignKey::class, "saveForeignKeyModel", saveForeignKeyModels.toString())
            }
            method("get${fieldName.capitalizeFirstLetter()}" returns
                    referencedDefinition.elementClassName modifiers publicFinal) {
                addStatement("return \$L", fieldName)
            }
            method("set${fieldName.capitalizeFirstLetter()}" returns
                    TypeName.VOID modifiers publicFinal) {
                addParameter(referencedDefinition.elementClassName, "param")
                addStatement("\$L = param", fieldName)
            }
        }
    }
}