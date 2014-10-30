package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.annotation.Table;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table
class ForeignModel extends TestModel1 {
    @Column(columnType = Column.FOREIGN_KEY,
            references =
                    {@ForeignKeyReference(columnName = "testmodel_id",
                            foreignColumnName = "name",
                            columnType = String.class)})
    TestModel1 testModel1;
}
