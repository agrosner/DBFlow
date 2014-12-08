package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

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
