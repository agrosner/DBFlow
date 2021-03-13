# Kotlin Support

While this library is written in Kotlin, there are a few Kotlin-specific nuances you must be aware of.

  1. Default Constructors
  2. Inline Classes
  3. `internal` modifier.
  


## Default Constructors

Currently, DBFlow only supports a default `constructor`. 

You must provide default values for all constructor arguments to generate a default constructor, or 
provide one yourself:

```kotlin

// OK
@Table(database = TestDatabase::class)
class Currency(@PrimaryKey(autoincrement = true) var id: Long = 0,
               @Column @Unique var symbol: String? = null,
               @Column var shortName: String? = null,
               @Column @Unique var name: String = "")
               
// Not OK
@Table(database = TestDatabase::class)
class Currency(@PrimaryKey(autoincrement = true) var id: Long,
              @Column @Unique var symbol: String?,
              @Column var shortName: String?,
              @Column @Unique var name: String)
              
// OK!
@Table(database = TestDatabase::class)
class Currency(@PrimaryKey(autoincrement = true) var id: Long,
               @Column @Unique var symbol: String?,
               @Column var shortName: String?,
               @Column @Unique var name: String) {
    constructor() : this(0L, null, null, "")               
               
}


```

### Inline Classes

A new feature of 1.3, DBFlow does not _quite_ support `inline` classes yet. 

Currently given two `inline` classes with a typical table name:
```kotlin
inline class Password(val value: String)
inline class Email(val value: String)

@Table(database = TestDatabase::class)
class UserInfo(@PrimaryKey
               var email: Email = Email(""),
               var password: Password = Password(""))
```

Kotlin will generate mangled setter method names so Java consumers cannot utilize them directly. DBFlow 
does not yet support "finding" these setters or know that they exist. The hash at the end of the method name 
might change or break in future versions, so there is a slight workaround.

To alleviate this issue:
  1. Define `@set:JvmName` on each property that is inline class with the expected set name
  2. Provide a default, visible constructor (as Kotlin makes this `private` by default).
  
  
```kotlin
inline class Password(val value: String)
inline class Email(val value: String)

@Table(database = TestDatabase::class)
class UserInfo(@PrimaryKey
               @set:JvmName("setEmail")
               var email: Email,
               @set:JvmName("setPassword")
               var password: Password) {
    constructor() : this(Email(""), Password(""))
}
```

### Internal

`internal` modifier is not quite supported yet with DBFlow for columns. Since it 
generates a different name for each build variant when compiled, we can not detect the getter/setter 
just yet automatically. It is _kind of_ allowed with DBFlow. You will have to 
override its generated getter/setter names to match the field name (which defeats purpose of `internal` from a Java consumer angle.)

```kotlin
@Table(database = TestDatabase::class)
class InternalClass internal constructor(@PrimaryKey
                                         @get:JvmName("getId")
                                         @set:JvmName("setId")
                                         internal var id: String = "")
```

Will compile, but now its not really different from a public property.