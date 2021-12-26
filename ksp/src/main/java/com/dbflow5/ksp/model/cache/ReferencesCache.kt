package com.dbflow5.ksp.model.cache

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.dbflow5.ksp.parser.extractors.FieldSanitizer
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName


private sealed interface ReferenceType {
    data class AllFromClass(val classType: TypeName) : ReferenceType
    data class SpecificReferences(val hash: Int) : ReferenceType
}

/**
 * Description: Looks up a specific reference for [FieldModel]
 * so it can grab its related references.
 */
class ReferencesCache(
    private val fieldSanitizer: FieldSanitizer,
) {

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

    fun resolveExistingFields(classType: TypeName): List<SingleFieldModel> {
        val nonNullVersion = classType.copy(false)
        return referenceMap.getOrPut(ReferenceType.AllFromClass(nonNullVersion)) {
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
        }
    }

    fun resolveComputedFields(
        ksType: KSType,
    ): List<SingleFieldModel> {
        val nonNullType = ksType.makeNotNullable()
        return referenceMap.getOrPut(ReferenceType.AllFromClass(nonNullType.toTypeName())) {
            val closest = nonNullType.declaration.closestClassDeclaration()
            closest?.let { fieldSanitizer.parse(it) }?.map {
                when (it) {
                    is ReferenceHolderModel -> it.references(this, nameToNest = it.name)
                    is SingleFieldModel -> listOf(it)
                }
            }?.flatten() ?: listOf()
        }
    }

    fun resolveReferencesOnExisting(
        list: List<ReferenceProperties>,
        classType: TypeName
    ): List<SingleFieldModel> {
        return referenceMap.getOrPut(
            ReferenceType.SpecificReferences(
                list.hashCode()
            )
        ) {
            this.resolveExistingFields(classType)
                .filter { field -> list.any { it.referencedName == field.name.shortName } }
        }
    }

    fun resolveReferencesOnComputedFields(
        list: List<ReferenceProperties>,
        ksType: KSType,
    ): List<SingleFieldModel> {
        return referenceMap.getOrPut(
            ReferenceType.SpecificReferences(
                list.hashCode()
            )
        ) {
            this.resolveComputedFields(ksType)
                .filter { field -> list.any { it.referencedName == field.name.shortName } }
        }
    }
}