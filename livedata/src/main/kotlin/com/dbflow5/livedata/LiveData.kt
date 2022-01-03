package com.dbflow5.livedata

import androidx.lifecycle.LiveData
import com.dbflow5.config.FlowManager
import com.dbflow5.config.databaseForTable
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.ModelQueriableEvalFn
import com.dbflow5.query.extractFrom
import kotlin.reflect.KClass

/**
 * Return a new [LiveData] instance. Specify using the [evalFn] what query to run.
 */
fun <T : Any, Q : ModelQueriable<T>, R> Q.toLiveData(evalFn: ModelQueriableEvalFn<T, R>): LiveData<R> =
    QueryLiveData(this, evalFn)

class QueryLiveData<T : Any, R : Any?>(
    private val modelQueriable: ModelQueriable<T>,
    private val evalFn: ModelQueriableEvalFn<T, R>
) : LiveData<R>() {

    private val associatedTables: Set<Class<*>> = modelQueriable.extractFrom()?.associatedTables
        ?: setOf(modelQueriable.table)

    private val onTableChangedObserver =
        object : OnTableChangedObserver(associatedTables.toList()) {
            override fun onChanged(tables: Set<Class<*>>) {
                if (tables.isNotEmpty()) {
                    evaluateEmission(tables.first().kotlin)
                }
            }
        }

    private fun evaluateEmission(table: KClass<*> = modelQueriable.table.kotlin) {
        databaseForTable(table)
            .transact {
                value = modelQueriable.evalFn(it)
            }
    }

    override fun onActive() {
        super.onActive()

        val db = FlowManager.getDatabaseForTable(associatedTables.first())
        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        observer.addOnTableChangedObserver(onTableChangedObserver)

        // trigger initial emission on active.
        evaluateEmission()
    }

    override fun onInactive() {
        super.onInactive()
        val db = FlowManager.getDatabaseForTable(associatedTables.first())
        val observer = db.tableObserver
        observer.removeOnTableChangedObserver(onTableChangedObserver)
    }
}
