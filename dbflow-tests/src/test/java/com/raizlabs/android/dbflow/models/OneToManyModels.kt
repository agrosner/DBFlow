package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.OneToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

@Table(database = TestDatabase::class)
class OneToManyModel(@PrimaryKey var name: String? = null) {

    var orders: List<TwoColumnModel>? = null

    @OneToMany(methods = arrayOf(OneToMany.Method.ALL), isVariablePrivate = true,
        variableName = "orders")
    fun getRelatedOrders(databaseWrapper: DatabaseWrapper): List<TwoColumnModel> {
        var localOrders = orders
        if (localOrders == null) {
            localOrders = (select from TwoColumnModel::class where id.greaterThan(3))
                .queryList(databaseWrapper)
        }
        orders = localOrders
        return localOrders
    }

}