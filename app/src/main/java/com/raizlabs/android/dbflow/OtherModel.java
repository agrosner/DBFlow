package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

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
