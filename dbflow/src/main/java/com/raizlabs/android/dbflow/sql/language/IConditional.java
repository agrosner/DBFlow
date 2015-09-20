package com.raizlabs.android.dbflow.sql.language;

/**
 * Description: Simple interface for objects that can be used as {@link Condition}. This class
 * takes no type parameters for primitive objects.
 */
public interface IConditional {

    Condition is(ITypeConditional conditional);

    Condition is(BaseModelQueriable baseModelQueriable);

    Condition isNull();

    Condition eq(ITypeConditional conditional);

    Condition eq(BaseModelQueriable baseModelQueriable);

    Condition isNotNull();

    Condition concatenate(ITypeConditional conditional);

    Condition isNot(ITypeConditional conditional);

    Condition isNot(BaseModelQueriable baseModelQueriable);

    Condition notEq(ITypeConditional conditional);

    Condition notEq(BaseModelQueriable baseModelQueriable);

    Condition like(ITypeConditional conditional);

    Condition like(BaseModelQueriable baseModelQueriable);

    Condition glob(ITypeConditional conditional);

    Condition glob(BaseModelQueriable baseModelQueriable);

    Condition greaterThan(ITypeConditional conditional);

    Condition greaterThan(BaseModelQueriable baseModelQueriable);

    Condition greaterThanOrEq(ITypeConditional conditional);

    Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable);

    Condition lessThan(ITypeConditional conditional);

    Condition lessThan(BaseModelQueriable baseModelQueriable);

    Condition lessThanOrEq(ITypeConditional conditional);

    Condition lessThanOrEq(BaseModelQueriable baseModelQueriable);

    Condition.Between between(ITypeConditional conditional);

    Condition.Between between(BaseModelQueriable baseModelQueriable);

    Condition.In in(ITypeConditional firstConditional, ITypeConditional... conditionals);

    Condition.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);

    Condition.In notIn(ITypeConditional firstConditional, ITypeConditional... conditionals);

    Condition.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);
}
