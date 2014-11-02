package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ModelView;
import com.grosner.dbflow.structure.BaseModelView;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@ModelView(query = "SELECT * FROM TestModel2 WHERE model_order > 5")
public class TestModelView extends BaseModelView<TestModel2> {
    @Column
    long model_order;
}
