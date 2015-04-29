package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description: Test to ensure that nullable and non-null and boxed primitive classes work as expected.
 */
@Table(databaseName = TestDatabase.NAME)
public class BoxedModel extends TestModel1 {

    @Column(notNull = true)
    @PrimaryKey
    Long id = 1L;

    @Column(notNull = true)
    public int integerPrimitiveFieldNotNull = 1;

    @Column(notNull = true)
    public Integer integerFieldNotNull = 1;

    @Column
    public Integer integerField = 1;

    @Column(notNull = true)
    public String stringFieldNotNull = "1";

    @Column
    public String stringField = "1";

}
