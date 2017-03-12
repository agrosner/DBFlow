package com.raizlabs.android.dbflow.rx2.language;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.IDelete;

public class RXDelete implements IDelete {

    private final Delete innerDelete;

    public RXDelete() {
        this.innerDelete = new Delete();
    }

    @Override
    public <TModel> RXFrom<TModel> from(Class<TModel> table) {
        return new RXFrom<>(this, table);
    }

    @Override
    public String getQuery() {
        return innerDelete.getQuery();
    }
}
