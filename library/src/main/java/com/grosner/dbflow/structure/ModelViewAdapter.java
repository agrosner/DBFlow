package com.grosner.dbflow.structure;

import android.database.Cursor;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class ModelViewAdapter<ModelClass extends Model, ModelViewClass extends BaseModelView<ModelClass>> {

    public abstract ModelViewClass loadFromCursor(Cursor cursor);

    public abstract String getCreationQuery();

    public abstract ConditionQueryBuilder<ModelViewClass> getPrimaryModelWhere(ModelViewClass modelView);

    public abstract ConditionQueryBuilder<ModelViewClass> getFullModelWhere(ModelViewClass model);

    public abstract boolean exists(ModelViewClass model);

    public abstract String getViewName();
}
