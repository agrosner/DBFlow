![Image](https://github.com/agrosner/DBFlow/blob/master/clear-river.jpg?raw=true)


DBFlow
======

A robust, powerful, and very simple ORM android database library.

The library eliminates the need for writing most SQL statements, writing ``ContentValues`` for every table, converting cursors into models, and so much more. 

Let DBFlow make SQL code _flow_ like a _steady_ stream so you can focus on your complex problem and not be hindered by repetitive code writing. 

This library is based on both [Active Android](https://github.com/pardom/ActiveAndroid) and [Sprinkles](https://github.com/emilsjolander/sprinkles), but takes the **best** of both while offering much more functionality and extensibility. 

**Please** note that this is not in full release yet, and thus is in **Alpha**. Also the code is **not** fully guaranteed yet, meaning classes, methods, or even whole packages may not exist in further updates until the library becomes stable.

## Features:

1. Loading and saving of Model objects. 
2. No need to define what Model classes to use - just implement the ```Model``` interface, extend ```BaseModel``` or extend ```BaseNotifiableModel``` .
3. Multiple primary key columns, handling complex foreign keys
4. Multi-database support fully baked in using the ```FlowManager```
5. Directly saving JSON to the database via ```JSONModel``` (with some minor caveats).
5. Database Views (Virtual tables)
6. Handling large amounts of database requests efficiently and effectively
7. Priority queuing of DB transactions through the ```DBTransactionQueue``` and the ```TransactionManager```
8. Migrations through both SQL files and inline code.
9. Complex queries wrapped in "builder" notation instead of using SQL strings.
10. ```TypeConverter``` that let **you** define how to store a particular class.
11. Powerful caching of reflection and repetitive queries to make this library _flow_ smoothly
12. And many more powerful features **baked** in.

## Documentation

1. [Getting Started](https://github.com/agrosner/DBFlow/wiki/Getting-Started)
3. [Building your database structure](https://github.com/agrosner/DBFlow/wiki/Building-your-database-structure)
4. [Basic Query Wrapping](https://github.com/agrosner/DBFlow/wiki/Basic-Query-Wrapping)
5. [Database Management](https://github.com/agrosner/DBFlow/wiki/Database-Management)
6. [Type Conversion](https://github.com/agrosner/DBFlow/wiki/Type-Conversion)
7. [Migrations](https://github.com/agrosner/DBFlow/wiki/Migrations)
8. [Observable Models](https://github.com/agrosner/DBFlow/wiki/Observable-Models)
9. [JSON Models](https://github.com/agrosner/DBFlow/wiki/JSON-Models)
