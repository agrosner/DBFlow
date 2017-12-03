package com.raizlabs.dbflow5.processor.definition

import com.grosner.kpoet.L
import com.grosner.kpoet.`@`
import com.grosner.kpoet.`fun`
import com.grosner.kpoet.`return`
import com.grosner.kpoet.field
import com.grosner.kpoet.final
import com.grosner.kpoet.member
import com.grosner.kpoet.modifiers
import com.grosner.kpoet.param
import com.grosner.kpoet.public
import com.grosner.kpoet.statement
import com.raizlabs.dbflow5.annotation.ForeignKey
import com.raizlabs.dbflow5.annotation.ManyToMany
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.processor.ClassNames
import com.raizlabs.dbflow5.processor.ProcessorManager
import com.raizlabs.dbflow5.processor.utils.annotation
import com.raizlabs.dbflow5.processor.utils.extractTypeNameFromAnnotation
import com.raizlabs.dbflow5.processor.utils.isNullOrEmpty
import com.raizlabs.dbflow5.processor.utils.lower
import com.raizlabs.dbflow5.processor.utils.toTypeElement
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

/**
 * Description: Generates the Model class that is used in a many to many.
 */
class ManyToManyDefinition(element: TypeElement, processorManager: ProcessorManager,
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

        databaseTypeName = element.extractTypeNameFromAnnotation<Table> { it.database }
        if (!thisColumnName.isNullOrEmpty() && !referencedColumnName.isNullOrEmpty()
                && thisColumnName == referencedColumnName) {
            manager.logError(ManyToManyDefinition::class, "The thisTableColumnName and referenceTableColumnName cannot be the same")
        }
    }

    fun prepareForWrite() {
        val databaseDefinition = manager.getDatabaseHolderDefinition(databaseTypeName)?.databaseDefinition
        if (databaseDefinition == null) {
            manager.logError("DatabaseDefinition was null for : $elementName")
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
            `fun`(referencedDefinition.elementClassName!!, "get${fieldName.capitalize()}") {
                modifiers(public, final)
                `return`(fieldName.L)
            }
            `fun`(TypeName.VOID, "set${fieldName.capitalize()}",
                    param(referencedDefinition.elementClassName!!, "param")) {
                modifiers(public, final)
                statement("$fieldName = param")
            }
        }
    }
}