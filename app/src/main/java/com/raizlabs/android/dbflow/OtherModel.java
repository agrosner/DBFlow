package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(tableName = "OtherModel", databaseName = AppDatabase.NAME)
@ModelContainer
public class OtherModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    @Column
    @ForeignKey(references =
            {@ForeignKeyReference(columnType = String.class,
                    columnName = "json",
                    foreignKeyColumnName = "name")})
    SecondModel candy;
}
