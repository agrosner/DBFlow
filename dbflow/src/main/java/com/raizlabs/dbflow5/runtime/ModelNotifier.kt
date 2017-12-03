package com.raizlabs.dbflow5.runtime

import com.raizlabs.dbflow5.structure.ChangeAction
import com.raizlabs.dbflow5.adapter.ModelAdapter

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>, action: ChangeAction)

    fun <T : Any> notifyTableChanged(table: Class<T>, action: ChangeAction)

    fun newRegister(): TableNotifierRegister
}
