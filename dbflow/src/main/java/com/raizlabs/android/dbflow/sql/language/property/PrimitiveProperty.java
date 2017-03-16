package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Description: Base class for primitive {@link IProperty} providing common set of methods.
 *
 * @author Andrew Grosner (fuzz)
 */
abstract class PrimitiveProperty<P extends IProperty> extends BaseProperty<P> {

    protected PrimitiveProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public Condition is(P property) {
        return column(nameAlias).is(property);
    }

    public Condition isNot(P property) {
        return column(nameAlias).isNot(property);
    }

    public Condition eq(P property) {
        return is(property);
    }

    public Condition notEq(P property) {
        return isNot(property);
    }

    public Condition greaterThan(P property) {
        return column(nameAlias).greaterThan(property);
    }

    public Condition greaterThanOrEq(P property) {
        return column(nameAlias).greaterThanOrEq(property);
    }

    public Condition lessThan(P property) {
        return column(nameAlias).lessThan(property);
    }

    public Condition lessThanOrEq(P property) {
        return column(nameAlias).lessThanOrEq(property);
    }

    @Override
    public P as(String aliasName) {
        return newPropertyInstance(table, nameAlias
            .newBuilder()
            .as(aliasName)
            .build());
    }

    @Override
    public P plus(IProperty iProperty) {
        return newPropertyInstance(table, NameAlias.joinNames(Condition.Operation.PLUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public P minus(IProperty iProperty) {
        return newPropertyInstance(table, NameAlias.joinNames(Condition.Operation.MINUS,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public P dividedBy(IProperty iProperty) {
        return newPropertyInstance(table, NameAlias.joinNames(Condition.Operation.DIVISION,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public P multipliedBy(IProperty iProperty) {
        return newPropertyInstance(table, NameAlias.joinNames(Condition.Operation.MULTIPLY,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public P mod(IProperty iProperty) {
        return newPropertyInstance(table, NameAlias.joinNames(Condition.Operation.MOD,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public P concatenate(IProperty iProperty) {
        return newPropertyInstance(table, NameAlias.joinNames(Condition.Operation.CONCATENATE,
            nameAlias.fullName(), iProperty.toString()));
    }

    @Override
    public P distinct() {
        return newPropertyInstance(table, getDistinctAliasName());
    }

    @Override
    public P withTable(NameAlias tableNameAlias) {
        return newPropertyInstance(table, nameAlias
            .newBuilder()
            .withTable(tableNameAlias.getQuery())
            .build());
    }

    /**
     * Used as convenience to allow all methods of this base class the contruct new instances
     * of its type.
     */
    protected abstract P newPropertyInstance(Class<?> table, NameAlias nameAlias);
}
