package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Simple interface for objects that can be used as {@link Operator}. This class
 * takes no type parameters for primitive objects.
 */
public interface IConditional extends Query {

    Operator is(IConditional conditional);

    Operator is(BaseModelQueriable baseModelQueriable);

    Operator isNull();

    Operator eq(IConditional conditional);

    Operator eq(BaseModelQueriable baseModelQueriable);

    Operator isNotNull();

    Operator concatenate(IConditional conditional);

    Operator isNot(IConditional conditional);

    Operator isNot(BaseModelQueriable baseModelQueriable);

    Operator notEq(IConditional conditional);

    Operator notEq(BaseModelQueriable baseModelQueriable);

    Operator like(IConditional conditional);

    Operator like(BaseModelQueriable baseModelQueriable);

    Operator glob(IConditional conditional);

    Operator glob(BaseModelQueriable baseModelQueriable);

    Operator like(String value);

    Operator notLike(String value);

    Operator glob(String value);

    Operator greaterThan(IConditional conditional);

    Operator greaterThan(BaseModelQueriable baseModelQueriable);

    Operator greaterThanOrEq(IConditional conditional);

    Operator greaterThanOrEq(BaseModelQueriable baseModelQueriable);

    Operator lessThan(IConditional conditional);

    Operator lessThan(BaseModelQueriable baseModelQueriable);

    Operator lessThanOrEq(IConditional conditional);

    Operator lessThanOrEq(BaseModelQueriable baseModelQueriable);

    Operator.Between between(IConditional conditional);

    Operator.Between between(BaseModelQueriable baseModelQueriable);

    Operator.In in(IConditional firstConditional, IConditional... conditionals);

    Operator.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);

    Operator.In notIn(IConditional firstConditional, IConditional... conditionals);

    Operator.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);
}
