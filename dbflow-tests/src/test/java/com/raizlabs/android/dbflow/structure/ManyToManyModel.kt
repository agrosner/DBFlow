package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*

/**
 * Description: Tests code gen on many to many.
 */
@Table(database = TestDatabase::class)
@ManyToMany(referencedTable = TestModel1::class)
@MultipleManyToMany(ManyToMany(referencedTable = TestModel2::class), ManyToMany(referencedTable = TestModel3::class))
class ManyToManyModel : BaseModel() {

    @PrimaryKey
    var name: String? = null

    @PrimaryKey
    var id: Int = 0

    @Column
    var anotherColumn: Char = ' '
}
