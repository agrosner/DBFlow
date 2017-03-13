package com.raizlabs.android.dbflow.structure.onetomany

import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel2
import com.raizlabs.android.dbflow.structure.TestModel2_Table

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class OneToManyModel : BaseModel() {

    @PrimaryKey
    var name: String? = null

    var orders: List<TestModel2>? = null

    @OneToMany(methods = arrayOf(OneToMany.Method.ALL), isVariablePrivate = true,
        variableName = "orders")
    fun getRelatedOrders(): List<TestModel2> {
        var localOrders = orders;
        if (localOrders == null) {
            localOrders = Select().from(TestModel2::class.java)
                .where(TestModel2_Table.model_order.greaterThan(3))
                .queryList()
        }
        orders = localOrders
        return localOrders
    }

}
