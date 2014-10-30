package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table
public class TestModel2 extends TestModel1 {
    @Column(name = "model_order")
    int order;
}
