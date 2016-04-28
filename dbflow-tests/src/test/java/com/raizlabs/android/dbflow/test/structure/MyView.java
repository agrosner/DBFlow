package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.annotation.ModelViewQuery;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelView(database = TestDatabase.class, name = "v_view")
public class MyView extends BaseModelView<MyView> {

    @ModelViewQuery
    public static final Query QUERY = SQLite.select().from(TestModel1.class);

    public enum TestEnum {YES, NO}

    @Column
    public TestEnum value;

    @Column
    public Boolean isSet;
}
