package com.dbflow5.model

import com.dbflow5.model.interop.ClassType
import com.dbflow5.model.interop.OriginatingFileType
import com.dbflow5.model.properties.FieldProperties
import com.dbflow5.model.properties.IndexProperties
import com.dbflow5.model.properties.NotNullProperties
import com.dbflow5.model.properties.ReferenceHolderProperties
import com.dbflow5.model.properties.UniqueProperties
import com.dbflow5.model.properties.nameWithFallback
import com.squareup.kotlinpoet.TypeName
import java.util.*


sealed interface FieldModel : ObjectModel {
    /**
     * The original name.
     */
    val name: NameModel

    /**
     * List of names, nested by call
     */
    val names: List<NameModel>

    /**
     * The declared type of the field.
     */
    val classType: TypeName
    val nonNullClassType: TypeName
        get() = classType.copy(nullable = false)

    val ksClassType: ClassType

    /**
     * If type is inline.
     */
    val isInlineClass: Boolean

    val isEnum: Boolean

    /**
     * If true, must exist in constructor, otherwise will be ignored.
     */
    val isVal: Boolean

    val fieldType: FieldType
    val properties: FieldProperties?
    val indexProperties: IndexProperties?
    val notNullProperties: NotNullProperties?
    val uniqueProperties: UniqueProperties?

    /**
     * This can be View, Normal, or Query. Based on [ClassModel]
     */
    val enclosingClassType: TypeName

    /**
     *  Join by name for properties.
     */
    val propertyName
        get() = properties.nameWithFallback(names.joinToString("_") { it.shortName })

    /**
     * [useLastNull] Last name if we want ? inserted
     */
    fun accessName(useLastNull: Boolean = false) = names
        .withIndex()
        .joinToString(".") { (index, value) ->
            if (index < names.size - 1 || useLastNull) {
                value.accessName
            } else {
                value.shortName
            }
        }

    val dbName
        get() = properties.nameWithFallback(propertyName)

    sealed interface FieldType {
        object Normal : FieldType
        data class PrimaryAuto(
            val isAutoIncrement: Boolean,
            val isRowId: Boolean,
        ) : FieldType
    }

}

/**
 * Description:
 */
data class SingleFieldModel(
    override val name: NameModel,

    /**
     * The declared type of the field.
     */
    override val classType: TypeName,
    override val fieldType: FieldModel.FieldType,
    override val properties: FieldProperties?,
    override val enclosingClassType: TypeName,
    override val names: List<NameModel> = listOf(name),
    override val isInlineClass: Boolean,
    override val isVal: Boolean,
    override val isEnum: Boolean,
    override val ksClassType: ClassType,
    override val originatingFile: OriginatingFileType?,
    override val indexProperties: IndexProperties?,
    override val notNullProperties: NotNullProperties?,
    override val uniqueProperties: UniqueProperties?,
) : ObjectModel, FieldModel

data class ReferenceHolderModel(
    override val name: NameModel,
    override val classType: TypeName,
    override val fieldType: FieldModel.FieldType,
    override val properties: FieldProperties?,
    val referenceHolderProperties: ReferenceHolderProperties,
    override val enclosingClassType: TypeName,
    override val names: List<NameModel> = listOf(name),
    val type: Type,
    override val isInlineClass: Boolean,
    override val ksClassType: ClassType,
    override val isVal: Boolean,
    val isColumnMap: Boolean,
    override val isEnum: Boolean,
    override val originatingFile: OriginatingFileType?,
    /**
     * Indexes on Reference models will apply to all reference fields.
     */
    override val indexProperties: IndexProperties?,
    override val notNullProperties: NotNullProperties?,
    override val uniqueProperties: UniqueProperties?,
) : ObjectModel, FieldModel {

    enum class Type {
        ForeignKey,

        /**
         * These are either ColumnMap or inline classes.
         */
        Computed,

        /**
         * These are specifically used in [OneToManyReference] definitions.
         */
        Reference,
    }

}

fun ReferenceHolderModel.toSingleModel() =
    SingleFieldModel(
        name = name,
        classType = classType,
        fieldType = fieldType,
        properties = properties,
        enclosingClassType = enclosingClassType,
        names = names,
        isInlineClass = isInlineClass,
        isVal = isVal,
        isEnum = isEnum,
        ksClassType = ksClassType,
        originatingFile = originatingFile,
        indexProperties = indexProperties,
        notNullProperties = notNullProperties,
        uniqueProperties = uniqueProperties,
    )
