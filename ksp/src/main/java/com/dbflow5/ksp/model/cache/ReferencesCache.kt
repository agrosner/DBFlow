package com.dbflow5.ksp.model.cache

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.dbflow5.ksp.parser.validation.ValidationExceptionProvider
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName


private sealed interface ReferenceType {
    data class AllFromClass(val classType: TypeName) : ReferenceType
    data class SpecificReferences(val hash: Int) : ReferenceType
    data class OneToManyReference(val classType: TypeName) : ReferenceType
}

/**
 * Description: Looks up a specific reference for [FieldModel]
 * so it can grab its related references.
 */
class ReferencesCache(
    private val fieldSanitizer: FieldSanitizer,
) {

    sealed interface Validation : ValidationExceptionProvider {

        data class MissingReferences(
            val type: String,
            val classType: TypeName,
            val field: FieldModel,
        ) : Validation {
            override val message: String = "Could not find $type references for $classType on " +
                "field ${field.name.shortName}."
        }
    }

    var allTables: List<ClassModel> = listOf()

    /**
     * If true, the field specified is a table.
     */
    fun isTable(fieldModel: FieldModel) = allTables.any {
        it.classType == fieldModel.classType.copy(
            nullable = false
        )
    }

    private val referenceMap = mutableMapOf<ReferenceType, List<SingleFieldModel>>()

    fun resolveExistingFields(
        fieldModel: FieldModel,
        classType: TypeName
    ): List<SingleFieldModel> {
        val nonNullVersion = classType.copy(false)
        return (referenceMap.getOrPut(ReferenceType.AllFromClass(nonNullVersion)) {
            allTables.firstOrNull { it.classType == nonNullVersion }
                ?.primaryFields?.map {
                    when (it) {
                        is ReferenceHolderModel -> it.references(
                            this,
                            nameToNest = it.name,
                        )
                        is SingleFieldModel -> listOf(it)
                    }
                }?.flatten() ?: listOf()
        }).takeIf { it.isNotEmpty() } ?: throw Validation.MissingReferences(
            "Primary",
            classType,
            fieldModel,
        ).exception
    }

    fun resolveOneToManyReferences(
        fieldModel: FieldModel,
        classType: TypeName
    ): List<SingleFieldModel> {
        val nonNullVersion = classType.copy(false)
        return (referenceMap.getOrPut(ReferenceType.OneToManyReference(nonNullVersion)) {
            allTables.firstOrNull { it.classType == nonNullVersion }
                // only look for foreign key types on the model as source for reference. it
                // is required to work.
                ?.fields?.filterIsInstance<ReferenceHolderModel>()?.map {
                    it.references(
                        this,
                        nameToNest = it.name,
                    )
                }?.flatten() ?: listOf()
        }).takeIf { it.isNotEmpty() } ?: throw Validation.MissingReferences(
            "ForeignKey",
            classType,
            fieldModel,
        ).exception
    }

    fun resolveComputedFields(
        fieldModel: FieldModel,
        ksType: KSType,
    ): List<SingleFieldModel> {
        val nonNullType = ksType.makeNotNullable()
        return (referenceMap.getOrPut(ReferenceType.AllFromClass(nonNullType.toTypeName())) {
            val closest = nonNullType.declaration.closestClassDeclaration()
            closest?.let { fieldSanitizer.parse(it) }?.map {
                when (it) {
                    is ReferenceHolderModel -> it.references(this, nameToNest = it.name)
                    is SingleFieldModel -> listOf(it)
                }
            }?.flatten() ?: listOf()
        }).takeIf { it.isNotEmpty() } ?: throw Validation.MissingReferences(
            "computed",
            nonNullType.toTypeName(),
            fieldModel,
        ).exception
    }

    fun resolveReferencesOnExisting(
        fieldModel: FieldModel,
        list: List<ReferenceProperties>,
        classType: TypeName
    ): List<SingleFieldModel> {
        return referenceMap.getOrPut(
            ReferenceType.SpecificReferences(
                list.hashCode()
            )
        ) {
            this.resolveExistingFields(fieldModel, classType)
                .filter { field -> list.any { it.referencedName == field.name.shortName } }
        }
    }

    fun resolveReferencesOnComputedFields(
        fieldModel: FieldModel,
        list: List<ReferenceProperties>,
        ksType: KSType,
    ): List<SingleFieldModel> {
        return referenceMap.getOrPut(
            ReferenceType.SpecificReferences(
                list.hashCode()
            )
        ) {
            this.resolveComputedFields(fieldModel, ksType)
                .filter { field -> list.any { it.referencedName == field.name.shortName } }
        }
    }
}