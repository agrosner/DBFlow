package com.raizlabs.android.dbflow.test.transaction;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@Table(database = TestDatabase.class, cachingEnabled = true)
public class TrCacheableModel extends TestModel1 {
}
