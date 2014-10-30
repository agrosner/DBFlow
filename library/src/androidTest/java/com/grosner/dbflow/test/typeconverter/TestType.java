package com.grosner.dbflow.test.typeconverter;

import android.location.Location;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.test.structure.TestModel1;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table
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
