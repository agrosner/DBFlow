package com.raizlabs.android.dbflow.test.sql.fast;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.contentobserver.ContentObserverModel;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import java.util.Date;

/**
 * Description: implementation for testing that utilizes speedy properties.
 */
@Table(database = TestDatabase.class, orderedCursorLookUp = true, assignDefaultValuesFromCursor = false)
public class FastModel extends BaseModel {

    @PrimaryKey
    long id;

    @Column
    String name;

    @ForeignKey
    ContentObserverModel contentObserverModel;

    @Column
    Date date;

    @ForeignKey
    TestModel1 testModel1ForeignKeyContainer;

}
