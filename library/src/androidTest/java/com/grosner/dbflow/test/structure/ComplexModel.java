package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.container.JSONModel;
import com.grosner.dbflow.structure.container.MapModel;
import com.grosner.dbflow.test.TestDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class ComplexModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column(columnType = Column.FOREIGN_KEY,
        references = {@ForeignKeyReference(columnName = "testmodel_id",
                columnType = String.class, foreignColumnName = "name")})
    JSONModel<TestModel1> testModel1;

    @Column(columnType = Column.FOREIGN_KEY,
    references = {@ForeignKeyReference(columnName = "mapmodel_id",
            columnType = String.class, foreignColumnName = "name")})
    MapModel<TestModel2> mapModel;

}
