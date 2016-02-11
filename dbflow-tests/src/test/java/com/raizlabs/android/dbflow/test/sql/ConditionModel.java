package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

@Table(database = TestDatabase.class)
public class ConditionModel extends TestModel1 {
    @Column
    long number;

    @Column
    int bytes;

    @Column
    double fraction;

    @Column
    float floatie;

    @Column
    short shortie;

    @Column
    byte bytie;

    @Column
    char charie;
}
