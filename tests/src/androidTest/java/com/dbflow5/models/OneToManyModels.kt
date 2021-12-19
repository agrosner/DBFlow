package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.*
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.query.select
import com.dbflow5.structure.BaseModel
import com.dbflow5.structure.oneToMany

@Table(database = TestDatabase::class)
class OneToManyModel(@PrimaryKey var name: String? = null) {

    @ColumnIgnore
    var orders: List<TwoColumnModel>? = null

    @ColumnIgnore
    var models: List<OneToManyBaseModel>? = null

    @get:OneToMany(oneToManyMethods = [OneToManyMethod.ALL])
    var simpleModels by oneToMany { select from OneToManyBaseModel::class }

    @get:OneToMany(oneToManyMethods = [OneToManyMethod.ALL])
    var setBaseModels by oneToMany { select from OneToManyBaseModel::class }

    @OneToMany(oneToManyMethods = [OneToManyMethod.ALL],
        variableName = "orders", efficientMethods = false)
    fun getRelatedOrders(wrapper: DatabaseWrapper): List<TwoColumnModel> {
        var localOrders = orders
        if (localOrders == null) {
            localOrders = (select from TwoColumnModel::class where id.greaterThan(3))
                .queryList(wrapper)
        }
        orders = localOrders
        return localOrders
    }

    @OneToMany(oneToManyMethods = [OneToManyMethod.DELETE],
        variableName = "models")
    fun getRelatedModels(wrapper: DatabaseWrapper): List<OneToManyBaseModel> {
        var localModels = models
        if (localModels == null) {
            localModels = (select from OneToManyBaseModel::class).queryList(wrapper)
        }
        models = localModels
        return localModels
    }


}

@Table(database = TestDatabase::class)
class OneToManyBaseModel(@PrimaryKey var id: Int = 0) : BaseModel()