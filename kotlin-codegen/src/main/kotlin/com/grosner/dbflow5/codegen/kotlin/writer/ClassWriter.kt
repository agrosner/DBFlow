package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.FieldPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.IndexPropertyWriter
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NameAllocator

/**
 * Writes adapter helpers used by plugin-generated model companions.
 * Column properties on [ClassNames.adapterCompanion] are internal stand-ins for binders.
 */
class ClassWriter(
    private val fieldPropertyWriter: FieldPropertyWriter,
    private val referencesCache: ReferencesCache,
    private val indexPropertyWriter: IndexPropertyWriter,
    private val tableSQLWriter: TableSQLWriter,
    private val tableBinderWriter: TableBinderWriter,
    private val primaryModelClauseWriter: PrimaryModelClauseWriter,
    private val autoIncrementUpdateWriter: AutoIncrementUpdateWriter,
    private val tableOpsWriter: TableOpsWriter,
    private val classAdapterWriter: ClassAdapterWriter,
    private val propertyGetterWriter: PropertyGetterWriter,
    private val queryOpsWriter: QueryOpsWriter,
    private val creationSQLWriter: CreationSQLWriter,
    private val nameAllocator: NameAllocator,
) : TypeCreator<ClassModel, FileSpec> {

    override fun create(model: ClassModel): FileSpec {
        return FileSpec.builder(model.name.packageName, "${model.name.shortName}_Adapter")
            .apply {
                addAnnotation(
                    AnnotationSpec.builder(ClassNames.OptIn)
                        .addMember("%T::class", ClassNames.InternalDBFlowApi)
                        .build()
                )
                addFunction(queryOpsWriter.create(model))
                if (!model.isQuery) {
                    addFunction(creationSQLWriter.create(model))
                    addProperty(propertyGetterWriter.create(model))
                }
                if (model.isNormal) {
                    addProperty(tableSQLWriter.create(model))
                    addProperty(tableBinderWriter.create(model))
                    addProperty(primaryModelClauseWriter.create(model))
                    addProperty(autoIncrementUpdateWriter.create(model))
                    addFunction(tableOpsWriter.create(model))
                }
                addFunction(classAdapterWriter.create(model))
                addProperty(companionOpsProperty(model, nameAllocator, referencesCache))
                addType(adapterCompanionFileObject(model.classType))
                val companionReceiver = ClassNames.adapterCompanion(model.classType)
                val propertyVisibility = if (model.isInternal) {
                    arrayOf(KModifier.INTERNAL)
                } else {
                    emptyArray()
                }
                model.flattenedFields(referencesCache).forEach { field ->
                    addProperty(
                        fieldPropertyWriter.create(model to field).toBuilder()
                            .receiver(companionReceiver)
                            .addModifiers(*propertyVisibility)
                            .build()
                    )
                }
                model.indexGroups.forEach { group ->
                    addProperty(
                        indexPropertyWriter.create(group).toBuilder()
                            .receiver(companionReceiver)
                            .addModifiers(*propertyVisibility)
                            .build()
                    )
                }
            }
            .build()
    }
}
