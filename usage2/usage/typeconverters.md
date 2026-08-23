# Type converters

Map a field that is not a SQLite type to one that is (`String`, number, `Blob`, …).

```kotlin
data class Money(val cents: Long)

@TypeConverter
class MoneyConverter : com.dbflow5.converter.TypeConverter<Long, Money> {
    override fun getDBValue(model: Money): Long = model.cents
    override fun getModelValue(data: Long): Money = Money(data)
}

@Table
data class Account(
    @PrimaryKey val id: Int = 0,
    val balance: Money,
)
```

The annotation registers the converter globally. Both methods are non-null; handle nullability in the model property instead.

Do not convert:

- Parameterized types (`List<T>`)
- One custom type into another custom type (chain two converters if you must)
- A `@Table` type (use `@ForeignKey`)

`allowedSubtypes` on `@TypeConverter` registers the same converter for subclasses.
