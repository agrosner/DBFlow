package com.grosner.dbflow.test.structure;

import android.location.Location;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.test.TestDatabase;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
@ContainerAdapter
public class TestPrimaryWhere extends TestModel1{
    @Column(columnType = Column.PRIMARY_KEY)
    Location location;
}
