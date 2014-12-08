package com.raizlabs.android.dbflow.test.typeconverter;

import android.location.Location;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
public class TestType extends TestModel1 {
    @Column
    Calendar calendar;

    @Column
    Date date;

    @Column
    java.sql.Date sqlDate;

    @Column
    JSONObject json;

    @Column
    Location location;
}
