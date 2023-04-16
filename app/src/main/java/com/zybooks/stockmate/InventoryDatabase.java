package com.zybooks.stockmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class InventoryDatabase extends SQLiteOpenHelper {

//    private static final String Log = "InventoryDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StockMate.db";

    private static InventoryDatabase sInventoryDatabase;

    private static final String SQL_CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " (" +
                    InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    InventoryContract.InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    InventoryContract.InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL);";

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " (" +
                    UserContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserContract.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                    UserContract.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL);";

    private static final String SQL_DELETE_INVENTORY_TABLE =
            "DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_USER_TABLE =
            "DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME;


    /**
     * Get singleton and create a new one if nonexistent
     * @param context the app's contest
     * @return Inventory database
     */
    public static InventoryDatabase getInstance(Context context) {
        Log.i("BUTTON","Get instance of database");
        if  (sInventoryDatabase == null) {
            sInventoryDatabase = new InventoryDatabase(context);
        }
        return sInventoryDatabase;
    }

    /**
     * Make the class private for singleton
     */
    // SQL statement to create the users table
    private InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_INVENTORY_TABLE);
        db.execSQL(SQL_DELETE_USER_TABLE);
        onCreate(db);
    }


    public List<Item> getItems() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_NAME,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY
        };

        Cursor cursor = db.query(
                InventoryContract.InventoryEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<Item> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_NAME));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_QUANTITY));

            items.add(new Item(id, name, quantity));
        }
        cursor.close();
        return items;
    }

    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();

        // Check if user already exists
        Cursor cursor = db.rawQuery("SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE " + UserContract.UserEntry.COLUMN_USERNAME + "=?", new String[]{email});
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false; // User already exists
        }
        cursor.close();

        // Add the new user to the database
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_USERNAME, name);
        values.put(UserContract.UserEntry.COLUMN_PASSWORD, password);

        long result = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);
        db.close();

        return result != -1; // Return true if the user was added successfully
    }

    public long addItem(String name, String description, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, name);
        values.put(InventoryContract.InventoryEntry.COLUMN_DESCRIPTION, description);
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);

        long newRowId = db.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        db.close();
        return newRowId;
    }

    // Define the update method
    public boolean updateItem(Item item) {
        SQLiteDatabase db = getWritableDatabase();

        // Create a ContentValues object to hold the new item data
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, item.getName());
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, item.getQuantity());

        // Define the selection criteria
        String selection = COLUMN + " = ?";
        String[] selectionArgs = { String.valueOf(itemId) };

        // Execute the update operation
        int rowsUpdated = db.update(TABLE_ITEMS, values, selection, selectionArgs);

        // Close the database connection
        db.close();

        // Return true if the update was successful, false otherwise
        return rowsUpdated > 0;
    }




}
