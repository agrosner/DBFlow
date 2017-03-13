package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ModelView
import com.raizlabs.android.dbflow.annotation.ModelViewQuery
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.TestDatabase

@ModelView(database = TestDatabase::class)
class TestModelView {

    @Column
    var model_order: Long = 0

    companion object {

        @JvmField
        @ModelViewQuery
        val QUERY: Query = Select().from(TestModel2::class.java)
                .where(TestModel2_Table.model_order.greaterThan(5))
    }
}
