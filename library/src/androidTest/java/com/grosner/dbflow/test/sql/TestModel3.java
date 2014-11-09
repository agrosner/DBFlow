package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ContainerAdapter;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.test.TestDatabase;
import com.grosner.dbflow.test.structure.TestModel1;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
@ContainerAdapter
public class TestModel3 extends TestModel1 {
    @Column
    String type;
}
