package com.raizlabs.android.dbflow.test.structure;

import android.location.Location;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class)
@ModelContainer
public class TestPrimaryWhere extends TestModel1 {
    @Column
    @PrimaryKey
    Location location;
}
