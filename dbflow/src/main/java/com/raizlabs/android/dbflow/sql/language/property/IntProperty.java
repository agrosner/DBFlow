package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description:
 */
public class IntProperty implements IProperty<IntProperty> {

    private final Class<? extends Model> table;
    protected final NameAlias nameAlias;

    public IntProperty(Class<? extends Model> table, NameAlias nameAlias) {
        this.table = table;
        this.nameAlias = nameAlias;
    }

    public IntProperty(Class<? extends Model> table, String columnName) {
        this(table, new NameAlias(columnName));
    }

    public IntProperty(Class<? extends Model> table, String columnName, String aliasName) {
        this(table, new NameAlias(columnName, aliasName));
    }

    @Override
    public IntProperty as(String aliasName) {
        return new IntProperty(table, nameAlias.getAliasNameRaw(), aliasName);
    }

    @Override
    public IntProperty distinct() {
        return new IntProperty(table, new NameAlias("DISTINCT " + nameAlias.getName(),
                nameAlias.getAliasPropertyRaw()).tickName(false));
    }

    @Override
    public IntProperty withTable() {
        return withTable(new NameAlias(FlowManager.getTableName(table)));
    }

    @Override
    public IntProperty withTable(NameAlias tableNameAlias) {
        NameAlias alias = new NameAlias(tableNameAlias.getAliasName() + "." + nameAlias.getName(), nameAlias.getAliasName());
        alias.tickName(false);
        return new IntProperty(table, alias);
    }

    @Override
    public NameAlias getNameAlias() {
        return null;
    }

    @Override
    public Class<? extends Model> getTable() {
        return null;
    }

    @Override
    public String getQuery() {
        return null;
    }
}
