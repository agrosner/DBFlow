package com.grosner.dbflow.app;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.structure.BaseModel;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class OtherModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;
}
