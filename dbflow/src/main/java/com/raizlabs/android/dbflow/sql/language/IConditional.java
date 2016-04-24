package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Simple interface for objects that can be used as {@link Condition}. This class
 * takes no type parameters for primitive objects.
 */
public interface IConditional extends Query {

    Condition is(IConditional conditional);

    Condition is(BaseModelQueriable baseModelQueriable);

    Condition isNull();

    Condition eq(IConditional conditional);

    Condition eq(BaseModelQueriable baseModelQueriable);

    Condition isNotNull();

    Condition concatenate(IConditional conditional);

    Condition isNot(IConditional conditional);

    Condition isNot(BaseModelQueriable baseModelQueriable);

    Condition notEq(IConditional conditional);

    Condition notEq(BaseModelQueriable baseModelQueriable);

    Condition like(IConditional conditional);

    Condition like(BaseModelQueriable baseModelQueriable);

    Condition glob(IConditional conditional);

    Condition glob(BaseModelQueriable baseModelQueriable);

    Condition like(String value);

    Condition glob(String value);

    Condition greaterThan(IConditional conditional);

    Condition greaterThan(BaseModelQueriable baseModelQueriable);

    Condition greaterThanOrEq(IConditional conditional);

    Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable);

    Condition lessThan(IConditional conditional);

    Condition lessThan(BaseModelQueriable baseModelQueriable);

    Condition lessThanOrEq(IConditional conditional);

    Condition lessThanOrEq(BaseModelQueriable baseModelQueriable);

    Condition.Between between(IConditional conditional);

    Condition.Between between(BaseModelQueriable baseModelQueriable);

    Condition.In in(IConditional firstConditional, IConditional... conditionals);

    Condition.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);

    Condition.In notIn(IConditional firstConditional, IConditional... conditionals);

    Condition.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);
}
