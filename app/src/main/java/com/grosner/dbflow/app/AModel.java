package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table
@ContainerAdapter
public class    AModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column
    long time;

    @Column(columnType = Column.FOREIGN_KEY,
            references = {@ForeignKeyReference(columnType = String.class, columnName = "otherModel", foreignColumnName = "name")})
    OtherModel model;

    @Column
    Date date;

}
