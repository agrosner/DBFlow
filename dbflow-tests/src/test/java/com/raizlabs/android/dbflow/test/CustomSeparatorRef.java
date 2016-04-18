package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.customseparator.CustomSeparatorClass;
import com.raizlabs.android.dbflow.test.customseparator.CustomSeparatorDatabase;

/**
 * Description:
 */
@Table(database = CustomSeparatorDatabase.class)
public class CustomSeparatorRef extends BaseModel {

    @PrimaryKey
    long id;

    @ForeignKey
    CustomSeparatorClass parent;
}
