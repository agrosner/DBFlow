package com.raizlabs.android.dbflow.test.structure;

import android.location.Location;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
@ModelContainer
public class TestPrimaryWhere extends TestModel1{
    @Column(columnType = Column.PRIMARY_KEY)
    Location location;
}
