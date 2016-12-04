package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ManyToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description: Test code gen of many to many with [ManyToMany.generateAutoIncrement] false.
 */
@Table(database = TestDatabase::class)
@ManyToMany(referencedTable = TestModel1::class, generateAutoIncrement = false, thisTableColumnName = "many", referencedTableColumnName = "test")
class ManyToManyModel2 : BaseModel() {

    @PrimaryKey
    var name: String? = null

    @PrimaryKey
    var id: Int = 0

    @Column
    var anotherColumn: Char = ' '
}
