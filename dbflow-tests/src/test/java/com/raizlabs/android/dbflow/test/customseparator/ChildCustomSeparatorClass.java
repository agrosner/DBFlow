package com.raizlabs.android.dbflow.test.customseparator;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = CustomSeparatorDatabase.class)
public class ChildCustomSeparatorClass extends BaseModel {

    @PrimaryKey
    int id;

    @ForeignKey
    CustomSeparatorClass parent;
}
