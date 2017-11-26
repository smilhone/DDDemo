package com.smilhone.doordashdemo.database;

import java.util.Map;

/**
 * Common functionality for DB Helper classes.
 *
 * Created by smilhone on 11/20/2017.
 */

public class BaseDBHelper {
    public static final long ROW_NOT_FOUND = -1;

    /**
     * Creates an inner join clause.
     *
     * @param leftTable The left table on the inner join.
     * @param rightTable The right table on the inner join.
     * @param leftColumn The column from the left table of the inner join.
     * @param rightColumn The column from the right table of the inner join.
     *
     * @return The SQL for an inner join between the two tables.
     */
    static String innerJoin(String leftTable, String rightTable, String leftColumn, String rightColumn) {
        return leftTable + " INNER JOIN " + rightTable + " ON "
                + leftTable + "." + leftColumn + " = " + rightTable + "." + rightColumn;
    }

    /**
     * Adds the columns into the look-up.  The added columns will have the qualified name '$tableName.$column AS $column'
     *
     * @param tableName The name of the table the columns belong to.
     * @param columns The columns to add.
     * @param lookup A Map containing the columns + their qualified name.
     */
    static void addColumnsToLookup(final String tableName, final String[] columns, Map<String, String> lookup) {
        for (final String column : columns) {
            lookup.put(column, getQualifiedName(tableName, column));
        }
    }

    /**
     * Creates a qualified name for the column.
     *
     * @param tableName The table the column belongs to.
     * @param columnName The name of the column.
     *
     * @return A qualified name in the format '$tableName.$columnName AS $columnName'
     */
    static String getQualifiedName(final String tableName, final String columnName) {
        return tableName + "." + columnName + " AS " + columnName;
    }
}
