package com.smilhone.doordashdemo.database;

/**
 * Created by smilhone on 11/20/2017.
 */

public class BaseDBHelper {
    public static final long ROW_NOT_FOUND = -1;

    static String innerJoin(String leftTable, String rightTable, String leftColumn, String rightColumn) {
        return leftTable + " INNER JOIN " + rightTable + " ON "
                + leftTable + "." + leftColumn + " = " + rightTable + "." + rightColumn;
    }
}
