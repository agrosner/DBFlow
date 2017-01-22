package com.raizlabs.android.dbflow.rx.language;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.sql.language.IUpdate;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Update;

/**
 * Description:
 */

public class RXUpdate<T> implements IUpdate<T> {

    private final Update<T> innerUpdate;

    public RXUpdate(Class<T> table) {
        this.innerUpdate = new Update<>(table);
    }

    @Override
    public String getQuery() {
        return innerUpdate.getQuery();
    }

    @Override
    public RXUpdate<T> conflictAction(ConflictAction conflictAction) {
        innerUpdate.conflictAction(conflictAction);
        return this;
    }

    @Override
    public RXUpdate<T> orRollback() {
        innerUpdate.orRollback();
        return this;
    }

    @Override
    public RXUpdate<T> orAbort() {
        innerUpdate.orAbort();
        return this;
    }

    @Override
    public RXUpdate<T> orReplace() {
        innerUpdate.orReplace();
        return this;
    }

    @Override
    public RXUpdate<T> orFail() {
        innerUpdate.orFail();
        return this;
    }

    @Override
    public RXUpdate<T> orIgnore() {
        innerUpdate.orIgnore();
        return this;
    }

    @Override
    public RXSet<T> set(SQLCondition... conditions) {
        return new RXSet<>(this, innerUpdate.getTable()).conditions(conditions);
    }
}
