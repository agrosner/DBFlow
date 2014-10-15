package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.structure.Model;

import java.util.Collection;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
public interface ProcessModel<ModelClass extends Model> {

    public void processModel(ModelClass model);
}
