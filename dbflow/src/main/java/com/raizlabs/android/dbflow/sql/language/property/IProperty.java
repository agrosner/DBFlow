package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Defines the base interface all property classes implement.
 */
public interface IProperty<P extends IProperty> {



    P as(String aliasName);

    P distinct();

    P withTable();

    P withTable(NameAlias tableNameAlias);

    Class<? extends Model> getTable();
}
