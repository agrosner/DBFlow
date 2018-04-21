package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.structure.BaseQueryModel

actual interface ModelQueriable<T : Any> : InternalModelQueriable<T> {

    /**
     * Returns a [List] based on the custom [TQueryModel] you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A list of custom models that are not tied to a table.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomList(queryModelClass: Class<TQueryModel>,
                                            databaseWrapper: DatabaseWrapper): MutableList<TQueryModel> =
        queryCustomList(queryModelClass.kotlin, databaseWrapper)

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A single model from the query.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomSingle(queryModelClass: Class<TQueryModel>,
                                              databaseWrapper: DatabaseWrapper): TQueryModel? =
        queryCustomSingle(queryModelClass.kotlin, databaseWrapper)

}