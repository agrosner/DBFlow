package com.raizlabs.android.dbflow.test.sql.index;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.Date;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME,
        indexGroups = {
                @IndexGroup(number = 1, name = "firstIndex"),
                @IndexGroup(number = 2, name = "secondIndex"),
                @IndexGroup(number = 3, name = "thirdIndex")
        })
public class IndexModel2 extends BaseModel {

    @Index(indexGroups = {1, 2, 3})
    @PrimaryKey
    @Column
    int id;

    @Index(indexGroups = 1)
    @Column
    String first_name;

    @Index(indexGroups = 2)
    @Column
    String last_name;

    @Index(indexGroups = {1, 3})
    @Column
    Date created_date;

    @Index(indexGroups = {2, 3})
    @Column
    boolean isPro;
}
