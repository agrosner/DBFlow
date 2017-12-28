package com.raizlabs.android.dbflow.runtime;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Interface for when a generic change on a table occurs.
 */
public interface OnTableChangedListener {

    /**
     * Called when table changes. This method can be called from the thread another then main thread.
     *
     * @param tableChanged The table that has changed. NULL unless version of app is {@link Build.VERSION_CODES#JELLY_BEAN}
     *                     or higher.
     * @param action       The action that occurred.
     */
    void onTableChanged(@Nullable Class<?> tableChanged, @NonNull BaseModel.Action action);
}
