package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(name = "AModel")
public class AModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY, name = "name")
    String name;

    @Column(name = "time")
    long time;
}
