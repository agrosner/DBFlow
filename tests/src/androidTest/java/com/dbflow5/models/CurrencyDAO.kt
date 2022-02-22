package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.coroutines.defer
import com.dbflow5.currencyAdapter
import com.dbflow5.paging.toDataSourceFactory
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.transaction.Transaction
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Deferred

/**
 *  Create this class in your own database module.
 */
interface DBProvider<out T : GeneratedDatabase> {

    val database: T

}

interface CurrencyDAO : DBProvider<TestDatabase> {

    fun coroutineStoreUSD(currency: Currency): Deferred<Currency> =
        database.beginTransactionAsync { currencyAdapter.save(currency) }.defer()

    /**
     *  Utilize coroutines package
     */
    fun coroutineRetrieveUSD(): Deferred<List<Currency>> =
        database.beginTransactionAsync {
            (currencyAdapter.select()
                where (Currency_Table.symbol eq "$")).list()
        }.defer()

    fun rxStoreUSD(currency: Currency): Single<Currency> =
        database.beginTransactionAsync { currencyAdapter.save(currency) }.asSingle()

    /**
     *  Utilize RXJava2 package.
     * Also can use asMaybe(), or asFlowable() (to register for changes and continue listening)
     */
    fun rxRetrieveUSD(): Single<List<Currency>> =
        database.beginTransactionAsync {
            (currencyAdapter.select()
                where (Currency_Table.symbol eq "$"))
                .list()
        }.asSingle()

    /**
     *  Utilize Vanilla Transactions.
     */
    fun retrieveUSD(): Transaction.Builder<TestDatabase, List<Currency>> =
        database.beginTransactionAsync {
            (currencyAdapter.select()
                where (Currency_Table.symbol eq "$"))
                .list()
        }

    /**
     *  Utilize Paging Library from paging artifact.
     */
    fun pagingRetrieveUSD() = (select from database.currencyAdapter
        where (Currency_Table.symbol eq "$"))
        .toDataSourceFactory(database)
}

fun provideCurrencyDAO(db: TestDatabase) = object : CurrencyDAO {
    override val database: TestDatabase = db
}