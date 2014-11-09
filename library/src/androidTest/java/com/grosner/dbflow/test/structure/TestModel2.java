package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.test.TestDatabase;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
public class TestModel2 extends TestModel1 {
    @Column(name = "model_order")
    int order;
}
