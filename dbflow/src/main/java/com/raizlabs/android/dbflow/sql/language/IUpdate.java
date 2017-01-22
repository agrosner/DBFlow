package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description:
 */

public interface IUpdate<T> extends Query {

    IUpdate<T> conflictAction(ConflictAction conflictAction);

    IUpdate<T> orRollback();

    IUpdate<T> orAbort();

    IUpdate<T> orReplace();

    IUpdate<T> orFail();

    IUpdate<T> orIgnore();

    ISet<T> set(SQLCondition... conditions);
}
