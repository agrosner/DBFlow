package com.raizlabs.android.dbflow.runtime;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: This class manages a single table, wrapping all of the
 * {@link com.raizlabs.android.dbflow.runtime.DatabaseManager} operations with the {@link ModelClass}
 */
public class TableManager<ModelClass extends Model> extends DatabaseManager<ModelClass> {

    private Class<ModelClass> mTableClass;

    /**
     * Constructs a new instance. If createNewQueue is true, it will create a new looper. So only use this
     * if you need to have a second queue to have certain transactions go faster. If you create a new queue,
     * it will use up much more memory.
     * @param createNewQueue Create a separate request queue from the shared one.
     * @param mTableClass The table class this manager corresponds to
     */
    public TableManager(boolean createNewQueue, Class<ModelClass> mTableClass) {
        super(mTableClass.getSimpleName(), createNewQueue);
        this.mTableClass = mTableClass;
    }

    public TableManager(Class<ModelClass> mTableClass) {
        super(mTableClass.getSimpleName(), false);
        this.mTableClass = mTableClass;
    }



}
