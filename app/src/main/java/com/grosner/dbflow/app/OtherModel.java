package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(value = "OtherModel")
@ContainerAdapter
public class OtherModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column(columnType = Column.FOREIGN_KEY,
    references = {@ForeignKeyReference(columnType = String.class,
    columnName = "json", foreignColumnName = "name")})
    SecondModel candy;
}
