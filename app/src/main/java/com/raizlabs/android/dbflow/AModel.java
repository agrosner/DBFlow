package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

/**
 * Description:
 */
@Table(database = AppDatabase.class)
@ModelContainer
public class AModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    @Column
    long time;

    @ForeignKey(references =
            {@ForeignKeyReference(columnType = String.class,
                    columnName = "otherModel",
                    foreignKeyColumnName = "name")})
    OtherModel model;

    @Column
    Date date;

    @ContainerKey
    String parseOnly;

    @ContainerKey
    long parseOnly2;

}
