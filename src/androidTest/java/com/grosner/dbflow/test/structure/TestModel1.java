package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
public class TestModel1 extends BaseModel {
    @Column(@ColumnType(ColumnType.PRIMARY_KEY))
    public
    String name;
}
