package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.modelAdapter
import com.dbflow5.coroutines.defer
import com.dbflow5.paging.QueryDataSource
import com.dbflow5.paging.toDataSourceFactory
import com.dbflow5.query.Where
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.transaction.Transaction
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Deferred

/**
 *  Create this class in your own database module.
 */
interface DBProvider<out T : DBFlowDatabase> {

    val database: T

}

interface CurrencyDAO : DBProvider<TestDatabase> {

    fun coroutineStoreUSD(currency: Currency): Deferred<Result<Currency>> =
        database.beginTransactionAsync { db ->
            modelAdapter<Currency>().save(currency, db)
        }.defer()

    /**
     *  Utilize coroutines package
     */
    fun coroutineRetrieveUSD(): Deferred<List<Currency>> =
        database.beginTransactionAsync {
            (select from Currency::class
                where (Currency_Table.symbol eq "$")).queryList(it)
        }.defer()

    fun rxStoreUSD(currency: Currency): Single<Result<Currency>> =
        database.beginTransactionAsync { db ->
            modelAdapter<Currency>().save(currency, db)
        }.asSingle()

    /**
     *  Utilize RXJava2 package.
     * Also can use asMaybe(), or asFlowable() (to register for changes and continue listening)
     */
    fun rxRetrieveUSD(): Single<List<Currency>> =
        database.beginTransactionAsync {
            (select from Currency::class
                where (Currency_Table.symbol eq "$"))
                .queryList(it)
        }.asSingle()

    /**
     *  Utilize Vanilla Transactions.
     */
    fun retrieveUSD(): Transaction.Builder<List<Currency>> =
        database.beginTransactionAsync {
            (select from Currency::class
                where (Currency_Table.symbol eq "$"))
                .queryList(it)
        }

    /**
     *  Utilize Paging Library from paging artifact.
     */
    fun pagingRetrieveUSD(): QueryDataSource.Factory<Currency, Where<Currency>> =
        (select from Currency::class
            where (Currency_Table.symbol eq "$"))
            .toDataSourceFactory(database)

}

fun provideCurrencyDAO(db: TestDatabase) = object : CurrencyDAO {
    override val database: TestDatabase = db
}