package com.dbflow5.ksp.writer

import com.dbflow5.annotation.Collate
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.model.references
import com.dbflow5.ksp.model.typeConverter
import com.dbflow5.model.ClassModel
import com.dbflow5.model.FieldModel
import com.dbflow5.model.ReferenceHolderModel
import com.dbflow5.model.SQLiteLookup
import com.dbflow5.model.SingleFieldModel
import com.dbflow5.model.properties.TableProperties
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.asTypeName

/**
 * Description:
 */
sealed interface FieldExtractor {

    val classModel: ClassModel

    val commaNames: String

    val updateName: String

    fun createName(
        sqLiteLookup: SQLiteLookup,
        typeConverterCache: TypeConverterCache
    ): String

    /**
     * Returned ? character.
     */
    val valuesName: String

    data class SingleFieldExtractor(
        private val field: SingleFieldModel,
        override val classModel: ClassModel,
    ) : FieldExtractor {

        override val valuesName: String
            get() = this.field.fieldType.let { type ->
                if (type is FieldModel.FieldType.PrimaryAuto
                    && type.isAutoIncrement
                    && this.field.notNullProperties != null
                ) {
                    // this patches in a null if value is 0 for non-null auto fields.
                    "nullif(?, 0)"
                } else "?"
            }
        override val commaNames: String = field.dbName.quoteIfNeeded()
        override val updateName: String = "${field.dbName.quoteIfNeeded()}=?"

        // TODO: use proper SQLiteType mapping.
        override fun createName(
            sqLiteLookup: SQLiteLookup,
            typeConverterCache: TypeConverterCache
        ): String {
            val value = sqLiteLookup.sqliteName(
                when {
                    field.hasTypeConverter(typeConverterCache) -> {
                        field.typeConverter(typeConverterCache).dataTypeName
                    }
                    field.isEnum -> String::class.asTypeName()
                    else -> field.nonNullClassType
                }
            ).sqliteName
            var retString = "${field.dbName.quoteIfNeeded()} $value"
            field.properties?.defaultValue?.let { value ->
                if (value.isNotBlank()) {
                    retString += " DEFAULT $value"
                }
            }

            field.fieldType.let { fieldType ->
                if (fieldType is FieldModel.FieldType.PrimaryAuto
                    && fieldType.isAutoIncrement
                ) {
                    retString += " PRIMARY KEY "
                    val properties = classModel.properties
                    if (properties is TableProperties &&
                        properties.primaryKeyConflict != ConflictAction.NONE
                    ) {
                        retString += "ON CONFLICT ${properties.primaryKeyConflict}"
                    }
                    retString += "AUTOINCREMENT"
                }
            }
            field.properties?.let { props ->
                if (props.length > -1) {
                    retString += "(${props.length})"
                }
                if (props.collate != Collate.NONE) {
                    retString += " COLLATE ${props.collate}"
                }
            }
            field.uniqueProperties?.let { props ->
                if (props.unique) {
                    retString += " UNIQUE ON CONFLICT ${props.conflictAction}"
                }
            }

            field.notNullProperties?.let { props ->
                retString += " NOT NULL ON CONFLICT ${props.conflictAction}"
            } ?: run {
                // not null, fail on conflict.
                if (!field.classType.isNullable) {
                    retString += " NOT NULL ON CONFLICT ${ConflictAction.FAIL}"
                }
            }

            return retString
        }
    }

    data class ForeignFieldExtractor(
        private val field: ReferenceHolderModel,
        private val referencesCache: ReferencesCache,
        override val classModel: ClassModel,
    ) : FieldExtractor {

        private val references = field.references(
            referencesCache,
            nameToNest = field.name,
        )
            .map {
                SingleFieldExtractor(it, classModel)
            }

        override val commaNames: String =
            references.joinToString {
                it.commaNames
            }

        override val updateName: String =
            references.joinToString {
                it.updateName
            }

        override fun createName(
            sqLiteLookup: SQLiteLookup,
            typeConverterCache: TypeConverterCache
        ): String =
            references.joinToString { it.createName(sqLiteLookup, typeConverterCache) }

        override val valuesName: String =
            references.joinToString { "?" }
    }
}