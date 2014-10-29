package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.container.JSONModel;
import com.grosner.dbflow.structure.container.ModelContainer;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(name = "AModel")
public class AModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column
    long time;

    @Column(columnType = Column.FOREIGN_KEY,
            references = {@ForeignKeyReference(columnType = String.class, columnName = "otherModel", foreignColumnName = "name")})
    OtherModel model;

}
