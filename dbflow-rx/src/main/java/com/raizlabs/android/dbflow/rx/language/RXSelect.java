package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.ISelect;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

public class RXSelect implements ISelect {

    private final Select innerSelect;

    public RXSelect(IProperty... properties) {
        innerSelect = new Select(properties);
    }

    @NonNull
    @Override
    public ISelect distinct() {
        innerSelect.distinct();
        return this;
    }

    @NonNull
    @Override
    public <TModel> RXFrom<TModel> from(Class<TModel> table) {
        return new RXFrom<>(this, table);
    }

    @Override
    public String getQuery() {
        return innerSelect.getQuery();
    }
}
