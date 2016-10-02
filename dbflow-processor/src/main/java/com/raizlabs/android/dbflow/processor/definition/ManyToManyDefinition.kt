package com.raizlabs.android.dbflow.processor.definition

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ManyToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.ProcessorManager
import com.raizlabs.android.dbflow.processor.utils.capitalizeFirstLetter
import com.raizlabs.android.dbflow.processor.utils.isNullOrEmpty
import com.raizlabs.android.dbflow.processor.utils.lower
import com.squareup.javapoet.*
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
    internal var generatedTableClassName: String
    internal var saveForeignKeyModels: Boolean = false
    internal var thisColumnName: String
    internal var referencedColumnName: String

    init {

        var clazz: TypeMirror? = null
        try {
            manyToMany.referencedTable
        } catch (mte: MirroredTypeException) {
            clazz = mte.typeMirror
        }
        referencedTable = TypeName.get(clazz)
        generateAutoIncrement = manyToMany.generateAutoIncrement
        generatedTableClassName = manyToMany.generatedTableClassName
        saveForeignKeyModels = manyToMany.saveForeignKeyModels

        sameTableReferenced = referencedTable == elementTypeName

        val table = element.getAnnotation(Table::class.java)
        try {
            table.database
        } catch (mte: MirroredTypeException) {
            databaseTypeName = TypeName.get(mte.typeMirror)
        }

        thisColumnName = manyToMany.thisTableColumnName
        referencedColumnName = manyToMany.referencedTableColumnName

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
                val referencedOutput = getElementClassName(manager.elements.getTypeElement(referencedTable.toString()))
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
            typeBuilder.addField(FieldSpec.builder(TypeName.LONG, "_id")
                    .addAnnotation(AnnotationSpec.builder(PrimaryKey::class.java)
                            .addMember("autoincrement", "true").build()).build())
            typeBuilder.addMethod(MethodSpec.methodBuilder("getId")
                    .returns(TypeName.LONG)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addStatement("return \$L", "_id").build())
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

        val fieldBuilder = FieldSpec.builder(referencedDefinition.elementClassName, fieldName)
                .addAnnotation(AnnotationSpec.builder(ForeignKey::class.java)
                        .addMember("saveForeignKeyModel", saveForeignKeyModels.toString()).build())
        if (!generateAutoIncrement) {
            fieldBuilder.addAnnotation(AnnotationSpec.builder(PrimaryKey::class.java).build())
        }
        typeBuilder.addField(fieldBuilder.build()).build()
        typeBuilder.addMethod(MethodSpec.methodBuilder("get" + fieldName.capitalizeFirstLetter())
                .returns(referencedDefinition.elementClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL).addStatement("return \$L", fieldName).build())
        typeBuilder.addMethod(MethodSpec.methodBuilder("set" + fieldName.capitalizeFirstLetter())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(referencedDefinition.elementClassName, "param")
                .addStatement("\$L = param", fieldName).build())
    }
}
