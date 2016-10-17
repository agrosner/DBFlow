# DBFlow 4.0 Migration guide

In 4.0, DBFlow has greatly improved its internals and flexibility in this release. We have removed the `Model` restriction, rewritten the annotation processor completely in Kotlin, and more awesome improvements.

_Major Changes In this release_
1. `PrimaryKey` can have `TypeConverters`, be table-based objects, and all kinds of objects. No real restrictions.
2. `ForeignKey` have been revamped to allow `stubbedRelationship`. This replaces `ForeignKeyContainer`.
3. `Model` interface now includes `load()` to enabled reloading very easily when fields change.
4. All `ModelContainer` implementation + support has been removed. A few reasons pushed the removal, including implementation. Since removing support, the annotation processor is cleaner, easier to maintain, and more streamlined. Also the support for it was not up to par, and by removing it, we can focus on improving the quality of the other features.
