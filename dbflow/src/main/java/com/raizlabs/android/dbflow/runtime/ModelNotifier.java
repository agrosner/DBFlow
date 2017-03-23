package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.SQLOperator;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Interface for defining how we notify model changes.
 */
public interface ModelNotifier {

    <T> void notifyModelChanged(@Nullable T model, @NonNull ModelAdapter<T> adapter,
                                @NonNull BaseModel.Action action);

    <T> void notifyModelChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action,
                                @Nullable Iterable<SQLOperator> conditions);

    <T> void notifyTableChanged(@NonNull Class<T> table, @NonNull BaseModel.Action action);

}
