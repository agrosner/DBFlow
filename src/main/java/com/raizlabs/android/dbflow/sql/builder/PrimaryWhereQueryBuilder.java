package com.raizlabs.android.dbflow.sql.builder;

import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.TableStructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Uses the primary columns of the {@link com.raizlabs.android.dbflow.structure.TableStructure}
 */
public class PrimaryWhereQueryBuilder<ModelClass extends Model> extends AbstractWhereQueryBuilder<ModelClass> {
    public PrimaryWhereQueryBuilder(Class<ModelClass> tableClass) {
        super(tableClass);
    }

    @Override
    protected List<String> getFieldNames(TableStructure<ModelClass> tableStructure) {
        return new ArrayList<String>(tableStructure.getPrimaryKeyNames());
    }
}
