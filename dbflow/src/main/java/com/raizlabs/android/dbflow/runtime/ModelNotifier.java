package com.raizlabs.android.dbflow.runtime;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Interface for defining how we notify model changes.
 */
public interface ModelNotifier {

    <T> void notifyModelChanged(@NonNull T model, @NonNull ModelAdapter<T> adapter,
                                @NonNull BaseModel.Action action);

    <T> void notifyTableChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action);

    TableNotifierRegister newRegister();
}
