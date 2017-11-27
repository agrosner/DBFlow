package com.raizlabs.android.dbflow.runtime

import com.raizlabs.android.dbflow.structure.ChangeAction
import com.raizlabs.android.dbflow.structure.ModelAdapter

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>, action: ChangeAction)

    fun <T : Any> notifyTableChanged(table: Class<T>, action: ChangeAction)

    fun newRegister(): TableNotifierRegister
}
