package com.dbflow5.livedata

import androidx.lifecycle.LiveData
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.ModelQueriableEvalFn
import com.dbflow5.query.extractFrom
import kotlin.reflect.KClass

/**
 * Return a new [LiveData] instance. Specify using the [evalFn] what query to run.
 */
fun <T : Any, Q : ModelQueriable<T>, R> Q.toLiveData(
    db: DBFlowDatabase,
    evalFn: ModelQueriableEvalFn<T, R>
): LiveData<R> =
    QueryLiveData(this, evalFn, db)

class QueryLiveData<T : Any, R : Any?>(
    private val modelQueriable: ModelQueriable<T>,
    private val evalFn: ModelQueriableEvalFn<T, R>,
    private val db: DBFlowDatabase,
) : LiveData<R>() {

    private val associatedTables: Set<KClass<*>> = modelQueriable.extractFrom()?.associatedTables
        ?: setOf(modelQueriable.table)

    private val onTableChangedObserver =
        object : OnTableChangedObserver(associatedTables.toList()) {
            override fun onChanged(tables: Set<KClass<*>>) {
                if (tables.isNotEmpty()) {
                    evaluateEmission()
                }
            }
        }

    private fun evaluateEmission() {
        db
            .beginTransactionAsync { modelQueriable.evalFn(db) }
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
