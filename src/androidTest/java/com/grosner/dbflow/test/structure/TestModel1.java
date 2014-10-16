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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestModel1 that = (TestModel1) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
