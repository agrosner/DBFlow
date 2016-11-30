package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class)
public class TestModel2 extends TestModel1 {
    @Column(name = "model_order")
    int order;

    public void setOrder(int order) {
        this.order = order;
    }
}
