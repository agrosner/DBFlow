package com.grosner.dbflow.sql;

import android.database.Cursor;

import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface Queriable<ModelClass extends Model> {

    public Cursor query();

    public List<ModelClass> queryList();

    public ModelClass querySingle();
}
