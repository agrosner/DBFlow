package com.raizlabs.android.dbflow.rx.language;

import com.raizlabs.android.dbflow.sql.language.ISelect;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

/**
 * Description:
 */

public class RXSelect implements ISelect {

    private final Select innerSelect;

    public RXSelect(IProperty... properties) {
        innerSelect = new Select(properties);
    }

    @Override
    public ISelect distinct() {
        innerSelect.distinct();
        return this;
    }

    @Override
    public <TModel> RXFrom<TModel> from(Class<TModel> table) {
        return new RXFrom<>(this, table);
    }

    @Override
    public String getQuery() {
        return innerSelect.getQuery();
    }
}
