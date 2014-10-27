package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface ProcessModel<ModelClass extends Model> {

    public void processModel(ModelClass model);
}
