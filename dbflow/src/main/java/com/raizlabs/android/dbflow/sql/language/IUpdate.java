package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.sql.Query;

public interface IUpdate<T> extends Query {

    @NonNull
    IUpdate<T> conflictAction(ConflictAction conflictAction);

    @NonNull
    IUpdate<T> orRollback();

    @NonNull
    IUpdate<T> orAbort();

    @NonNull
    IUpdate<T> orReplace();

    @NonNull
    IUpdate<T> orFail();

    @NonNull
    IUpdate<T> orIgnore();

    @NonNull
    ISet<T> set(SQLCondition... conditions);
}
