# sqlinsert
Repo for eclipse sql-insert plugin

This plugin is meant to allow you to export a result set from an SQL query as a set of SQL Insert statements.
This is useful for creating database scripts where you are trying to copy data to a new database table.

This useful functionality isn't available in stock Eclipse, hopefully it will be added some time soon.

Usage:
======

1. From the SQL Sheet, perform a query such as SELECT * from MYTABLE;
2. From the Results window, right-click and under the Export Results, select the Format as "Export as SQL Insert"
3. Choose the filename, and click Ok.
4. Voila, SQL Insert script should be created.

NB:
==

This plugin is rudimentary. It is expecting the query to be of the form
SELECT * from SOME_TABLE [where etc. etc ].

It expects the form so it can extract the table_name for the final query. Unfortunately the tablename isn't visible at the point where the output is meant to be generated.

Hope this helps you like it helped me!
Kiren