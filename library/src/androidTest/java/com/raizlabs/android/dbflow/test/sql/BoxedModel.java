package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description: Test to ensure that nullable and non-null and boxed primitive classes work as expected.
 */
@Table(databaseName = TestDatabase.NAME)
public class BoxedModel extends TestModel1 {

    @Column(columnType = Column.PRIMARY_KEY, notNull = true)
    Long id = 1L;

    @Column(notNull = true)
    public short shortPrimitiveFieldNotNull = 1;

    @Column(notNull = true)
    public Short shortFieldNotNull = 1;

    @Column()
    public Short shortField = 1;

    @Column(notNull = true)
    public int integerPrimitiveFieldNotNull = 1;

    @Column(notNull = true)
    public Integer integerFieldNotNull = 1;

    @Column()
    public Integer integerField = 1;

    @Column(notNull = true)
    public long longPrimitiveFieldNotNull = 1L;

    @Column(notNull = true)
    public Long longFieldNotNull = 1L;

    @Column()
    public Long longField = 1L;

    @Column(notNull = true)
    public float floatPrimitiveFieldNotNull = 1.0f;

    @Column(notNull = true)
    public Float floatFieldNotNull = 1.0f;

    @Column()
    public Float floatField = 1.0f;

    @Column(notNull = true)
    public double doublePrimitiveFieldNotNull = 1.0;

    @Column(notNull = true)
    public Double doubleFieldNotNull = 1.0;

    @Column()
    public Double doubleField = 1.0;

    @Column(notNull = true)
    public String stringFieldNotNull = "1";

    @Column()
    public String stringField = "1";

}
