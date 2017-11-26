package com.smilhone.doordashdemo.common;

import android.database.Cursor;

/**
 * Helper methods for cursors.
 *
 * Created by smilhone on 11/20/2017.
 */

public class CursorUtils {
    /**
     * Closes the cursor.
     *
     * @param cursor The cursor to close.
     */
    public static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
