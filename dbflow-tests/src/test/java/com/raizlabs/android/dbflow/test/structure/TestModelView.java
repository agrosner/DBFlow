package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelView(database = TestDatabase.class)
public class TestModelView extends BaseModelView {

    @ModelViewQuery
    public static final Query QUERY = new Select().from(TestModel2.class)
            .where(TestModel2_Table.model_order.greaterThan(5));

    @Column
    long model_order;
}
