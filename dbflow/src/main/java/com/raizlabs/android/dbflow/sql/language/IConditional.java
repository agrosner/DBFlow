package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Simple interface for objects that can be used as {@link Operator}. This class
 * takes no type parameters for primitive objects.
 */
public interface IConditional extends Query {

    @NonNull
    Operator is(@NonNull IConditional conditional);

    @NonNull
    Operator is(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator isNull();

    @NonNull
    Operator eq(@NonNull IConditional conditional);

    @NonNull
    Operator eq(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator isNotNull();

    @NonNull
    Operator concatenate(@NonNull IConditional conditional);

    @NonNull
    Operator isNot(@NonNull IConditional conditional);

    @NonNull
    Operator isNot(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator notEq(@NonNull IConditional conditional);

    @NonNull
    Operator notEq(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator like(@NonNull IConditional conditional);

    @NonNull
    Operator like(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator notLike(@NonNull IConditional conditional);

    @NonNull
    Operator notLike(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator glob(@NonNull IConditional conditional);

    @NonNull
    Operator glob(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator like(@NonNull String value);

    @NonNull
    Operator notLike(@NonNull String value);

    @NonNull
    Operator glob(@NonNull String value);

    @NonNull
    Operator greaterThan(@NonNull IConditional conditional);

    @NonNull
    Operator greaterThan(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator greaterThanOrEq(@NonNull IConditional conditional);

    @NonNull
    Operator greaterThanOrEq(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator lessThan(@NonNull IConditional conditional);

    @NonNull
    Operator lessThan(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator lessThanOrEq(@NonNull IConditional conditional);

    @NonNull
    Operator lessThanOrEq(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator.Between between(@NonNull IConditional conditional);

    @NonNull
    Operator.Between between(@NonNull BaseModelQueriable baseModelQueriable);

    @NonNull
    Operator.In in(@NonNull IConditional firstConditional, @NonNull IConditional... conditionals);

    @NonNull
    Operator.In in(@NonNull BaseModelQueriable firstBaseModelQueriable,
                   @NonNull BaseModelQueriable... baseModelQueriables);

    @NonNull
    Operator.In notIn(@NonNull IConditional firstConditional, @NonNull IConditional... conditionals);

    @NonNull
    Operator.In notIn(@NonNull BaseModelQueriable firstBaseModelQueriable,
                      @NonNull BaseModelQueriable... baseModelQueriables);

    @NonNull
    Operator plus(@NonNull BaseModelQueriable value);

    @NonNull
    Operator minus(@NonNull BaseModelQueriable value);

    @NonNull
    Operator div(@NonNull BaseModelQueriable value);

    @NonNull
    Operator times(@NonNull BaseModelQueriable value);

    @NonNull
    Operator rem(@NonNull BaseModelQueriable value);
}
