package com.dbflow5.ksp.writer

import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.ForeignKeyModel
import com.dbflow5.ksp.model.ReferencesCache
import com.dbflow5.ksp.model.SingleFieldModel
import com.dbflow5.ksp.model.properties.nameWithFallback

/**
 * Description:
 */
sealed interface FieldExtractor {

    val commaNames: String

    val updateName: String

    val createName: String

    /**
     * Returned ? character.
     */
    val valuesName: String
        get() = "?"

    data class SingleFieldExtractor(
        private val field: SingleFieldModel,
    ) : FieldExtractor {


        override val commaNames: String = field.dbName
        override val updateName: String = "${field.dbName}=?"

        // TODO: use proper SQLiteType mapping.
        override val createName: String = "${field.dbName} ${field.classType}"
    }

    data class ForeignFieldExtractor(
        private val field: ForeignKeyModel,
        private val referencesCache: ReferencesCache,
    ) : FieldExtractor {

        private val references = field.references(
            referencesCache,
            namePrefix = field.dbName
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

        override val createName: String =
            references.joinToString { it.createName }
        override val valuesName: String =
            references.joinToString { "?" }
    }
}