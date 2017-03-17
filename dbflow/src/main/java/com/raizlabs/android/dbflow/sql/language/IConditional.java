package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Simple interface for objects that can be used as {@link Operator}. This class
 * takes no type parameters for primitive objects.
 */
public interface IConditional extends Query {

    @NonNull
    Operator is(IConditional conditional);

    @NonNull
    Operator is(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator isNull();

    @NonNull
    Operator eq(IConditional conditional);

    @NonNull
    Operator eq(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator isNotNull();

    @NonNull
    Operator concatenate(IConditional conditional);

    @NonNull
    Operator isNot(IConditional conditional);

    @NonNull
    Operator isNot(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator notEq(IConditional conditional);

    @NonNull
    Operator notEq(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator like(IConditional conditional);

    @NonNull
    Operator like(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator glob(IConditional conditional);

    @NonNull
    Operator glob(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator like(String value);

    @NonNull
    Operator notLike(String value);

    @NonNull
    Operator glob(String value);

    @NonNull
    Operator greaterThan(IConditional conditional);

    @NonNull
    Operator greaterThan(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator greaterThanOrEq(IConditional conditional);

    @NonNull
    Operator greaterThanOrEq(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator lessThan(IConditional conditional);

    @NonNull
    Operator lessThan(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator lessThanOrEq(IConditional conditional);

    @NonNull
    Operator lessThanOrEq(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator.Between between(IConditional conditional);

    @NonNull
    Operator.Between between(BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator.In in(IConditional firstConditional, IConditional... conditionals);

    @NonNull
    Operator.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);

    @NonNull
    Operator.In notIn(IConditional firstConditional, IConditional... conditionals);

    @NonNull
    Operator.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);

    @NonNull
    Operator plus(IConditional value);

    @NonNull
    Operator minus(IConditional value);

    @NonNull
    Operator div(IConditional value);

    @NonNull
    Operator times(IConditional value);

    @NonNull
    Operator rem(IConditional value);

    @NonNull
    Operator plus(BaseModelQueriable value);

    @NonNull
    Operator minus(BaseModelQueriable value);

    @NonNull
    Operator div(BaseModelQueriable value);

    @NonNull
    Operator times(BaseModelQueriable value);

    @NonNull
    Operator rem(BaseModelQueriable value);
}
