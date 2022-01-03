package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.modelAdapter
import com.dbflow5.paging.QueryDataSource
import com.dbflow5.paging.toDataSourceFactory
import com.dbflow5.query.Where
import com.dbflow5.query.select
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.rx3.rxSingle

/**
 *  Create this class in your own database module.
 */
interface DBProvider<out T : DBFlowDatabase> {

    val database: T

}

interface CurrencyDAO : DBProvider<TestDatabase> {

    fun coroutineStoreUSD(currency: Currency): Flow<Result<Currency>> =
        flow {
            emit(database.executeTransaction { db ->
                modelAdapter<Currency>().save(currency, db)
            })
        }

    /**
     *  Utilize coroutines package
     */
    fun coroutineRetrieveUSD(): Flow<MutableList<Currency>> =
        flow {
            emit(database.executeTransaction {
                (select from Currency::class
                    where (Currency_Table.symbol eq "$")).queryList(it)
            })
        }

    fun rxStoreUSD(currency: Currency): Single<Result<Currency>> =
        rxSingle {
            database.executeTransaction { db ->
                modelAdapter<Currency>().save(currency, db)
            }
        }

    /**
     *  Utilize RXJava2 package.
     * Also can use asMaybe(), or asFlowable() (to register for changes and continue listening)
     */
    fun rxRetrieveUSD(): Single<MutableList<Currency>> =
        rxSingle {
            database.executeTransaction {
                (select from Currency::class
                    where (Currency_Table.symbol eq "$"))
                    .queryList(it)
            }
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