package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ModelView;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.test.TestDatabase;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@ModelView(query = "SELECT * FROM TestModel2 WHERE model_order > 5", databaseName = TestDatabase.NAME)
public class TestModelView extends BaseModelView<TestModel2> {
    @Column
    long model_order;
}
