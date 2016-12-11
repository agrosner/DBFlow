package com.raizlabs.android.dbflow.test.sql.index

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase
import java.util.*

/**
 * Description:
 */
@Table(database = TestDatabase::class, indexGroups = arrayOf(
        IndexGroup(number = 1, name = "firstIndex"),
        IndexGroup(number = 2, name = "secondIndex"),
        IndexGroup(number = 3, name = "thirdIndex")))
class IndexModel2 : BaseModel() {

    @Index(indexGroups = intArrayOf(1, 2, 3))
    @PrimaryKey
    @Column
    var id: Int = 0

    @Index(indexGroups = intArrayOf(1))
    @Column
    var first_name: String? = null

    @Index(indexGroups = intArrayOf(2))
    @Column
    var last_name: String? = null

    @Index(indexGroups = intArrayOf(1, 3))
    @Column
    var created_date: Date? = null

    @Index(indexGroups = intArrayOf(2, 3))
    @Column
    var isPro: Boolean = false

    @ForeignKey(stubbedRelationship = true,
            references = arrayOf(ForeignKeyReference(columnName = "entityID",
                    foreignKeyColumnName = "id")))
    @Index(indexGroups = intArrayOf(2))
    var indexModel: IndexModel? = null
}
