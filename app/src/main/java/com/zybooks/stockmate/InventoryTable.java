package com.zybooks.stockmate;

import android.provider.BaseColumns;

// Inventory table contract class
public final class InventoryTable {
    private InventoryTable() {}

    public final class InventoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_QUANTITY = "quantity";

//        public static final String CREATE_INVENTORY_TABLE =
//                "CREATE TABLE " + TABLE_NAME + " (" +
//                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                        COLUMN_NAME + " TEXT NOT NULL, " +
//                        COLUMN_DESCRIPTION + " TEXT, " +
//                        COLUMN_QUANTITY + " REAL NOT NULL);";
    }


}
