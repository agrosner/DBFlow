package com.raizlabs.android.dbflow.test.typeconverter;

import android.location.Location;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

@Table(database = TestDatabase.class)
public class TestType extends TestModel1 {

    @Column
    boolean nativeBoolean;

    @Column
    Boolean aBoolean;

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

    @Column(typeConverter = CustomBooleanConverter.class)
    Boolean thisHasCustom;

    @Column(typeConverter = EnumOverriderConverter.class)
    private EnumOverriderConverter.TestEnum testEnum;

    @Column
    Blobable blobable;

    @ForeignKey
    UPrimary primary;

    public EnumOverriderConverter.TestEnum getTestEnum() {
        return testEnum;
    }

    public void setTestEnum(EnumOverriderConverter.TestEnum testEnum) {
        this.testEnum = testEnum;
    }
}
