package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.FieldPropertyWriter
import com.grosner.dbflow5.codegen.kotlin.writer.classwriter.IndexPropertyWriter
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

/**
 * Description:
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
) : TypeCreator<ClassModel, FileSpec> {

    override fun create(model: ClassModel): FileSpec {
        return FileSpec.builder(model.name.packageName, model.generatedClassName.shortName)
            .apply {
                addFunction(queryOpsWriter.create(model))
                if (!model.isQuery) {
                    addFunction(creationSQLWriter.create(model))
                }
                if (model.isNormal) {
                    addProperty(tableSQLWriter.create(model))
                    addProperty(tableBinderWriter.create(model))
                    addProperty(primaryModelClauseWriter.create(model))
                    addProperty(autoIncrementUpdateWriter.create(model))
                    addProperty(propertyGetterWriter.create(model))
                    addFunction(tableOpsWriter.create(model))
                }
                addFunction(classAdapterWriter.create(model))

                addType(TypeSpec.objectBuilder(model.generatedClassName.className)
                    .addModifiers(if (model.isInternal) KModifier.INTERNAL else KModifier.PUBLIC)
                    .addSuperinterface(
                        ClassNames.adapterCompanion(
                            model.classType,
                        )
                    )
                    .apply {
                        addProperty(
                            PropertySpec.builder(
                                "table", KClass::class.asClassName()
                                    .parameterizedBy(model.classType)
                            )
                                .addModifiers(KModifier.OVERRIDE)
                                .getter(
                                    FunSpec.getterBuilder()
                                        .addStatement(
                                            "return %T::class",
                                            model.classType
                                        )
                                        .build()
                                )
                                .build()
                        )
                        model.flattenedFields(referencesCache).forEach { field ->
                            addProperty(fieldPropertyWriter.create(model to field))
                        }
                        model.indexGroups.forEach {
                            addProperty(indexPropertyWriter.create(it))
                        }
                    }
                    .build()
                )
            }
            .build()

    }
}
