package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.BaseModelView
import com.raizlabs.android.dbflow.test.TestDatabase

@ModelView(database = TestDatabase::class, priority = 2)
class BlobTestView : BaseModelView() {

    @Column
    var data: Blob? = null


    @Table(database = TestDatabase::class)
    class DataTable : BaseModel() {

        @PrimaryKey(autoincrement = true)
        var id: Long = 0

        @Column
        var data: Blob? = null
    }

    companion object {

        @JvmField
        @ModelViewQuery
        val QUERY: Query = Select(DataTable_Table.data).from(DataTable::class.java)
    }
}
