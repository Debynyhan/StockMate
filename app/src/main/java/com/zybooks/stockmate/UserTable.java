package com.zybooks.stockmate;

import android.provider.BaseColumns;

// User table contract class
public final class UserTable {
    private UserTable() {}

    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";

        public static final String COLUMN_REGISTERED = "registered";

        private static final String SQL_CREATE_USER_TABLE =
                "CREATE TABLE " + UserTable.UserEntry.TABLE_NAME + " (" +
                        UserTable.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        UserTable.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                        UserTable.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                        UserTable.UserEntry.COLUMN_REGISTERED + " INTEGER NOT NULL DEFAULT 0);";

    }
}
