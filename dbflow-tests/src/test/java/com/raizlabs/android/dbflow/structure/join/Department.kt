package com.raizlabs.android.dbflow.structure.join

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description: Taken from http://www.tutorialspoint.com/sqlite/sqlite_using_joins.htm
 */
@Table(database = TestDatabase::class)
class Department : BaseModel() {

    @Column
    @PrimaryKey
    var id: Long = 0

    @Column
    var dept: String? = null

    @Column
    var emp_id: Long = 0
}
