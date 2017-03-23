package com.raizlabs.android.dbflow.runtime;

import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Interface for defining how we notify model changes.
 */
public interface ModelNotifier {

    <T> void notifyModelChanged(T model, ModelAdapter<T> adapter, BaseModel.Action action);
}
