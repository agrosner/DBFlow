package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table
public class TestModel1 extends BaseModel {
    @Column(columnType = Column.PRIMARY_KEY)
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
