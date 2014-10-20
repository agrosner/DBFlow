![Image](https://github.com/agrosner/DBFlow/blob/master/clear-river.jpg?raw=true)


DBFlow (BETA)
======

A robust, powerful, and very simple ORM android database library.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on both [Active Android](https://github.com/pardom/ActiveAndroid) and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of both while offering much more functionality and extensibility. 

**Please** note that this is not in full release yet, and thus is in **Beta**. Also the code is **not** fully guaranteed yet, but I promise to be nicer in changes from now on.

## Features:

[Getting Started](https://github.com/agrosner/DBFlow/wiki/Getting-Started)

### Efficiency
[Building your database structure](https://github.com/agrosner/DBFlow/wiki/Building-your-database-structure)

[Transactions](https://github.com/agrosner/DBFlow/wiki/Database-Transactions) wrap batch operations in one database transaction and run all on a priority queue.

eliminates repetitive code, built to handle large DB operations, caching where needed

### Extensibility
customizable interfaces for many aspects of the library

```Model```: The main table class

```Transaction```: Runs a [transaction](https://github.com/agrosner/DBFlow/wiki/Database-Transactions) on the ```DBTransactionQueue```

```Migration```: Define how you wish to modify the database

```Queriable```: Custom definition for how to retrieve data from the database

```ModelChangeListener```: Listens for operations on a ```Model``` and provides a callback for when they change. [Example](https://github.com/agrosner/DBFlow/wiki/Observable-Models)

```TypeConverter```: Allows non-model classes to define how they save to a singular column in the database ([here](https://github.com/agrosner/DBFlow/wiki/Type-Conversion)).

### Power
[SQL-lite query wrapping](https://github.com/agrosner/DBFlow/wiki/Basic-Query-Wrapping)

```Select```, ```Update```, ```Delete``` are all supported.

[**Multiple database support**](https://github.com/agrosner/DBFlow/wiki/Multiple-Databases)

```FlowManager```: Manages a database. To enable multiple databases, call ```FlowManager.setMultipleDatabases(true)``` and specify in each's ```DBConfiguration``` which ```Model``` class to use (two dbs cannot share the same model class).

[JSON Models](https://github.com/agrosner/DBFlow/wiki/JSON-Models)

```JSONModel```: Maps a Json object to a ```Model``` in the database. It will directly save the JSON into the DB using a ```Model``` class as its blueprint.

### Ease
[Migration handling](https://github.com/agrosner/DBFlow/wiki/Migrations)

```BaseMigration```: Provides a base implementation to execute some operation on the database

```AlterTableMigration```: When you want to change table's name or add columns

```UpdateTableMigration```: Define the ```Update``` to run for a specific DB version

### Familiarity
Handling DB tables like a java ```List``` with the ```FlowTableList``` ([here](https://github.com/agrosner/DBFlow/wiki/Tables-as-Lists))

### Flexibility
Can use the [SQL wrapping language](https://github.com/agrosner/DBFlow/wiki/Basic-Query-Wrapping), ```FlowCursorList```, or ```TransactionManager``` to perform DB operations. Each one serves a specific purpose. 

