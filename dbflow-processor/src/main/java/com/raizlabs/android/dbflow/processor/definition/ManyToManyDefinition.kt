package com.raizlabs.android.dbflow.processor.definition

import com.grosner.kpoet.*
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
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

/**
 * Description: Generates the Model class that is used in a many to many.
 */
class ManyToManyDefinition @JvmOverloads constructor(element: TypeElement, processorManager: ProcessorManager,
                                                     manyToMany: ManyToMany = element.annotation<ManyToMany>()!!)
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

        element.annotation<Table>()?.let { table ->
            try {
                table.database
            } catch (mte: MirroredTypeException) {
                databaseTypeName = TypeName.get(mte.typeMirror)
            }
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
        typeBuilder.apply {
            addAnnotation(AnnotationSpec.builder(Table::class.java)
                    .addMember("database", "\$T.class", databaseTypeName).build())

            val referencedDefinition = manager.getTableDefinition(databaseTypeName, referencedTable)
            val selfDefinition = manager.getTableDefinition(databaseTypeName, elementTypeName)

            if (generateAutoIncrement) {
                addField(field(`@`(PrimaryKey::class) { this["autoincrement"] = "true" }, TypeName.LONG, "_id").build())

                `fun`(TypeName.LONG, "getId") {
                    modifiers(public, final)
                    `return`("_id")
                }
            }

            referencedDefinition?.let { appendColumnDefinitions(this, it, 0, referencedColumnName) }
            selfDefinition?.let { appendColumnDefinitions(this, it, 1, thisColumnName) }
        }
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
            `field`(referencedDefinition.elementClassName!!, fieldName) {
                if (!generateAutoIncrement) {
                    `@`(PrimaryKey::class)
                }
                `@`(ForeignKey::class) { member("saveForeignKeyModel", saveForeignKeyModels.toString()) }
            }
            `fun`(referencedDefinition.elementClassName!!, "get${fieldName.capitalizeFirstLetter()}") {
                modifiers(public, final)
                `return`(fieldName.L)
            }
            `fun`(TypeName.VOID, "set${fieldName.capitalizeFirstLetter()}",
                    param(referencedDefinition.elementClassName!!, "param")) {
                modifiers(public, final)
                statement("$fieldName = param")
            }
        }
    }
}