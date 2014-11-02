package com.grosner.dbflow.test.structure;

import android.location.Location;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.dbflow.annotation.Table;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table
@ContainerAdapter
public class TestPrimaryWhere extends TestModel1{
    @Column(columnType = Column.PRIMARY_KEY)
    Location location;
}
