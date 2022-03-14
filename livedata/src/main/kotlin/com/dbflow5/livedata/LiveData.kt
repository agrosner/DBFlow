package com.dbflow5.livedata

import androidx.lifecycle.LiveData
import com.dbflow5.database.GeneratedDatabase
import com.dbflow5.database.beginTransactionAsync
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.HasAssociatedAdapters
import com.dbflow5.query.SelectResult

/**
 * Return a new [LiveData] instance. Specify using the [evalFn] what query to run.
 */
fun <Table : Any, Result, Q> Q.toLiveData(
    db: GeneratedDatabase,
    selectResultFn: suspend SelectResult<Table>.() -> Result,
): LiveData<Result>
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAssociatedAdapters =
    QueryLiveData(this, selectResultFn, db)

class QueryLiveData<Table : Any, Result, Q>(
    private val executable: Q,
    private val selectResultFn: suspend SelectResult<Table>.() -> Result,
    private val db: GeneratedDatabase,
) : LiveData<Result>()
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAssociatedAdapters {

    private val onTableChangedObserver = OnTableChangedObserver(executable.associatedAdapters) {
        evaluateEmission()
    }

    private fun evaluateEmission() {
        db
            .beginTransactionAsync { executable.execute().selectResultFn() }
            .enqueue { _, r -> value = r }
    }

    override fun onActive() {
        super.onActive()

        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        observer.addOnTableChangedObserver(onTableChangedObserver)

        // trigger initial emission on active.
        evaluateEmission()
    }

    override fun onInactive() {
        super.onInactive()
        val observer = db.tableObserver
        observer.removeOnTableChangedObserver(onTableChangedObserver)
    }
}
