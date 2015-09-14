package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelView;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelView(query = "SELECT * FROM TestModel2 WHERE model_order > 5", database = TestDatabase.class)
public class TestModelView extends BaseModelView<TestModel2> {
    @Column
    long model_order;
}
