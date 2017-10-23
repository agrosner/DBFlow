package com.raizlabs.android.dbflow.runtime

import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.ModelAdapter

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>, action: BaseModel.Action)

    fun <T : Any> notifyTableChanged(table: Class<T>, action: BaseModel.Action)

    fun newRegister(): TableNotifierRegister
}
