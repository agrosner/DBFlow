package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.coroutines.defer
import com.dbflow5.paging.QueryDataSource
import com.dbflow5.paging.toDataSourceFactory
import com.dbflow5.query.Where
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.transaction.Transaction
import io.reactivex.Single
import kotlinx.coroutines.Deferred

/**
 *  Create this class in your own database module.
 */
interface DBProvider<out T : DBFlowDatabase> {

    val database: T

}

interface CurrencyDAO : DBProvider<TestDatabase> {

    /**
     *  Utilize coroutines package
     */
    fun coroutineRetrieveUSD(): Deferred<MutableList<Currency>> =
            database.beginTransactionAsync {
                (select from Currency::class
                        where (Currency_Table.symbol eq "$")).queryList(it)
            }.defer()

    /**
     *  Utilize RXJava2 package.
     * Also can use asMaybe(), or asFlowable() (to register for changes and continue listening)
     */
    fun rxRetrieveUSD(): Single<MutableList<Currency>> =
            database.beginTransactionAsync {
                (select from Currency::class
                        where (Currency_Table.symbol eq "$"))
                        .queryList(it)
            }.asSingle()

    /**
     *  Utilize Vanilla Transactions.
     */
    fun retrieveUSD(): Transaction.Builder<MutableList<Currency>> =
            database.beginTransactionAsync {
                (select from Currency::class
                        where (Currency_Table.symbol eq "$"))
                        .queryList(it)
            }

    /**
     *  Utilize Paging Library from paging artifact.
     */
    fun pagingRetrieveUSD(): QueryDataSource.Factory<Currency, Where<Currency>> = (select from Currency::class
            where (Currency_Table.symbol eq "$"))
            .toDataSourceFactory(database)

}

fun provideCurrencyDAO(db: TestDatabase) = object : CurrencyDAO {
    override val database: TestDatabase = db
}