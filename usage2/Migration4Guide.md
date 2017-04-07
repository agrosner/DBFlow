# DBFlow 4.0 Migration guide

In 4.0, DBFlow has greatly improved its internals and flexibility in this release. We have removed the `Model` restriction, rewritten the annotation processor completely in Kotlin, and more awesome improvements.

_Major Changes In this release_

1. `PrimaryKey` can have `TypeConverters`, be table-based objects, and all kinds of objects. No real restrictions.

2. `ForeignKey` have been revamped to allow `stubbedRelationship`. This replaces `ForeignKeyContainer`.

3. `Model` interface now includes `load()` to enabled reloading very easily when fields change.

4. All `ModelContainer` implementation + support has been removed. A few reasons pushed the removal, including implementation. Since removing support, the annotation processor is cleaner, easier to maintain, and more streamlined. Also the support for it was not up to par, and by removing it, we can focus on improving the quality of the other features.

5. The annotation processor has been rewritten in Kotlin! By doing so, we reduced the code by ~13%.

6. Removed the `Model` restriction on tables. If you leave out extending `BaseModel`, you _must_ interact with the `ModelAdapter`.

7. We generate much less less code than 3.0. Combined the `_Table` + `_Adapter` into the singular `_Table` class, which contains both `Property` + all of the regular `ModelAdapter` methods. To ease the transition to 4.0, it is named `_Table` but extends `ModelAdapter`. So most use cases / interactions will not break.

8. `Condition` are now `Operator`, this includes `SQLCondition` -> `SQLOperator`, `ConditionGroup` -> `OperatorGroup`. `Operator` are now typed and safer to use.
  1. `Operator` now also have `div`, `times`, `rem`, `plus` and `minus` methods.

9. Property class changes:
  1. All primitive `Property` classes have been removed. We already boxed the values internally anyways so removing them cut down on method count and maintenance.
  2. `BaseProperty` no longer needs to exist, so all of it's methods now exist in `Property`
  3. `mod` method is now `rem` (remainder) method to match Kotlin 1.1's changes.
  4. `dividedBy` is now `div` to match Kotlin operators.
  5. `multipliedBy` is now `times` to match Kotlin operators.

10. Rewrote all Unit tests to be more concise, better tested, and cleaner.

11. A lot of bug fixes

12. Kotlin:
  1. Added more Kotlin extensions.
  2. Most importantly you don't need to use `BaseModel`/`Model` at all anymore if you so choose. There are `Model`-like extension methods that supply the `Model` methods.
  3. Updated to version 1.1.1

13. RXJava1 and RXJava2 support! Can now write queries that return `Observable` and more.
