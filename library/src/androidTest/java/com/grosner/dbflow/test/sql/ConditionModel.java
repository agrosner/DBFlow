package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.test.TestDatabase;
import com.grosner.dbflow.test.structure.TestModel1;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
public class ConditionModel extends TestModel1 {
    @Column
    long number;

    @Column
    int bytes;

    @Column
    double fraction;
}
