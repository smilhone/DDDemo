package com.smilhone.doordashdemo.common;

import android.database.Cursor;

/**
 * Created by smilhone on 11/20/2017.
 */

public class CursorUtils {
    public static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
