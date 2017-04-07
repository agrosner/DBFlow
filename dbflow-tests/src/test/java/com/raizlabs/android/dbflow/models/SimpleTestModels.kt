package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class SimpleModel(@PrimaryKey var name: String? = "")

@QueryModel(database = TestDatabase::class)
class SimpleCustomModel(@Column var name: String? = "")

@Table(database = TestDatabase::class, insertConflict = ConflictAction.FAIL, updateConflict = ConflictAction.FAIL)
class NumberModel(@PrimaryKey var id: Int = 0)

@Table(database = TestDatabase::class)
class TwoColumnModel(@PrimaryKey var name: String? = "", @Column var id: Int = 0)

@Table(database = TestDatabase::class, allFields = true)
open class AllFieldsModel(@PrimaryKey var name: String? = null,
                          var count: Int = 0,
                          @Column(getterName = "getTruth")
                          var truth: Boolean = false,
                          internal val finalName: String = "",
                          @ColumnIgnore private val hidden: Int = 0) {

    companion object {

        // example field to ensure static not used.
        var COUNT: Int = 0
    }
}

@Table(database = TestDatabase::class, allFields = true)
class SubclassAllFields(@Column var order: Int = 0) : AllFieldsModel()