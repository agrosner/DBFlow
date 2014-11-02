package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.structure.BaseModel;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(databaseName = "SecondApp")
public class SecondModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;
}
