package com.raizlabs.android.dbflow.test.structure.onetomany

import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel2
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class OneToManyModel : BaseModel() {

    @PrimaryKey
    var name: String? = null

    var orders: List<TestModel2>? = null

    @OneToMany(methods = arrayOf(OneToMany.Method.ALL))
    fun getOrders(): List<TestModel2> {
        if (orders == null) {
            orders = Select().from(TestModel2::class.java)
                    .where(TestModel2_Table.model_order.greaterThan(3))
                    .queryList()
        }
        return orders
    }

}
