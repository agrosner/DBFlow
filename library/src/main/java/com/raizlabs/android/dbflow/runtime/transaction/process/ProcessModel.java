package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ProcessModel<ModelClass extends Model> {

    public void processModel(ModelClass model);
}
