package com.zybooks.stockmate;

import android.provider.BaseColumns;

// User table contract class
public final class UserTable {
    private UserTable() {}

    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "username";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_PASSWORD = "password";



    }
}
