package com.dbflow5.ksp.writer

import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ReferenceHolderModel
import com.dbflow5.ksp.model.SQLiteLookup
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.model.typeConverter
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.asTypeName

/**
 * Description:
 */
sealed interface FieldExtractor {

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
        get() = "?"

    data class SingleFieldExtractor(
        private val field: SingleFieldModel,
    ) : FieldExtractor {


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
            if (field.fieldType is FieldModel.FieldType.PrimaryAuto
                && field.fieldType.isAutoIncrement
            ) {
                retString += " PRIMARY KEY "
                // TODO: conflict action
                retString += "AUTOINCREMENT"
            }
            return retString
        }
    }

    data class ForeignFieldExtractor(
        private val field: ReferenceHolderModel,
        private val referencesCache: ReferencesCache,
    ) : FieldExtractor {

        private val references = field.references(
            referencesCache,
            nameToNest = field.name,
        )
            .map {
                SingleFieldExtractor(it)
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