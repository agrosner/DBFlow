package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;
import com.grosner.dbflow.structure.container.JSONModel;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(name = "OtherModel")
@ContainerAdapter
public class OtherModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column(columnType = Column.FOREIGN_KEY,
    references = {@ForeignKeyReference(columnType = String.class,
    columnName = "json", foreignColumnName = "name")})
    String candy;
}
