package com.raizlabs.android.dbflow.test.structure.caching

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class, cachingEnabled = true)
class MultipleCacheableModel : BaseModel() {

    @PrimaryKey
    var latitude: Double = 0.toDouble()

    @PrimaryKey
    var longitude: Double = 0.toDouble()

    @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "associatedModel",
        columnType = String::class, foreignKeyColumnName = "name",
        referencedFieldIsPrivate = true)))
    var associatedModel: TestModel1? = null

    companion object {

        @JvmField
        @MultiCacheField
        val multiKeyCacheModel: IMultiKeyCacheConverter<String> = IMultiKeyCacheConverter { values -> "(" + values[0] + "," + values[1] + ")" }
    }

}
