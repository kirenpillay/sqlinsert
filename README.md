Welcome to the sqlinsert wiki!

# Introduction

This Eclipse plugin is meant to allow you to export a result set from an SQL query as a set of SQL Insert statements. This is useful for creating database scripts to copy data to a new database.

This basic functionality isn't available in stock-standard Eclipse, hopefully it will be added some time soon.

![Eclipse sql insert plugin](https://github.com/kirenpillay/sqlinsert/blob/master/images/sqlinsert.png)

# Usage


1. From the SQL Sheet, perform a query such as SELECT * from MYTABLE;
2. From the Results window, right-click and under the Export Results, select the Format as "Export as SQL Insert"
3. Choose the filename, and click Ok.
4. Voila, SQL Insert script should be created with contents as shown in example below:
    INSERT INTO INFO (id,NAME,SURNAME,AGE) VALUES (1,'Kiren','Pillay','41');
    INSERT INTO INFO (id,NAME,SURNAME,AGE) VALUES (3,'Joe ','Blog','35');


# Alternatives
Looks like someone has already solved this problem! 
See [sql-insert-generator](https://marketplace.eclipse.org/content/sql-insert-script-generator)

I think my implementation, while basic and bare-bones is a bit more intuitive to use, let me know what you think.

# NB

This plugin is rudimentary. It is expecting the query to be of the form
SELECT * from SOME_TABLE [where etc. etc ].

It expects this form so it can extract the table_name for the result file's insert statements. (Unfortunately the tablename isn't visible at the point where the output is meant to be generated, so had to use this method).

Hope this helps you like it helped me!
Kiren

# Installation

Unzip into your eclipse directory.

Supported Versions:
1. Luna

Unsupported:
1. Mars