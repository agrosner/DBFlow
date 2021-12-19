package com.dbflow5.ksp.model.cache

import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.properties.ReferenceProperties
import com.squareup.kotlinpoet.TypeName


private sealed interface ReferenceType {
    data class AllFromClass(val classType: TypeName) : ReferenceType
    data class SpecificReferences(val hash: Int) : ReferenceType
}

/**
 * Description: Looks up a specific reference for [FieldModel]
 * so it can grab its related references.
 */
class ReferencesCache {

    var allTables: List<ClassModel> = listOf()

    /**
     * If true, the field specified is a table.
     */
    fun isTable(fieldModel: FieldModel) = allTables.any {
        it.classType == fieldModel.classType
    }

    private val referenceMap = mutableMapOf<ReferenceType, List<SingleFieldModel>>()

    operator fun get(classType: TypeName): List<SingleFieldModel> {
        val nonNullVersion = classType.copy(false)
        return referenceMap.getOrPut(ReferenceType.AllFromClass(nonNullVersion)) {
            allTables.firstOrNull { it.classType == nonNullVersion }
                ?.fields?.map {
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

    fun references(
        list: List<ReferenceProperties>,
        classType: TypeName
    ): List<SingleFieldModel> {
        return referenceMap.getOrPut(
            ReferenceType.SpecificReferences(
                list.hashCode()
            )
        ) {
            this[classType].filter { field -> list.any { it.referencedName == field.name.shortName } }
        }
    }
}