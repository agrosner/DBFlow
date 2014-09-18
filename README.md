DBFlow
======

A robust, powerful, and very simple ORM android database library.

The library eliminates the need for writing SQL statements, writing ``ContentValues`` for every table, converting cursors into data, and so much more. 

Let DBFlow make SQL code _flow_ like a steady stream and allow you to focus on your problems, not be hindered by repetitive code writing. 

## Features:

1. Loading and saving of Model objects. 
2. Multiple primary key columns, handling complex foreign keys
3. Multi-database support fully baked in
4. Database Views
5. Handling large amounts of database requests efficiently and effectively
6. Priority queuing of DB transactions
7. No need to define what Model classes to use - just implement the ``Model``` interface or extend ```BaseModel```.
8. Migrations
9. Complex queries wrapped in "builder" notatation instead of using SQL strings.
10. ```TypeConverter``` that let **you** define how to store a particular class.
11. And many more powerful features **baked** in.

## Documentation

1. [Getting Started](https://github.com/agrosner/DBFlow/wiki/Getting-Started)
2. [Building your database structure](https://github.com/agrosner/DBFlow/wiki/structure)
3. [Database Management](https://github.com/agrosner/DBFlow/wiki/dbmanagement)
4. [Type Conversion](https://github.com/agrosner/DBFlow/wiki/typeconverters)
