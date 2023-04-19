package com.zybooks.stockmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class InventoryDatabase extends SQLiteOpenHelper {

    // Database name and version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StockMate.db";
    private static final int MAX_INPUT_LENGTH = 20;

    private static final String TAG = "InventoryDatabase";
    private static final int MIN_PASSWORD_LENGTH = 10;


    // Singleton instance of the database
    private static InventoryDatabase sInventoryDatabase;

    // SQL statements to create and delete tables
    private static final String SQL_CREATE_INVENTORY_TABLE =
            "CREATE TABLE " + InventoryTable.InventoryEntry.TABLE_NAME + " (" +
                    InventoryTable.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    InventoryTable.InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    InventoryTable.InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL);";

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE " + UserTable.UserEntry.TABLE_NAME + " (" +
                    UserTable.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserTable.UserEntry.COLUMN_USERNAME + " TEXT NOT NULL, " +
                    UserTable.UserEntry.COLUMN_PASSWORD + " TEXT NOT NULL);";

    private static final String SQL_DELETE_INVENTORY_TABLE =
            "DROP TABLE IF EXISTS " + InventoryTable.InventoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_USER_TABLE =
            "DROP TABLE IF EXISTS " + UserTable.UserEntry.TABLE_NAME;


    /**
     * This method get the singleton instance of the database
     * @param context the app's context
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
     * Constructor for the InventoryDatabase class
     * @param context the app's context
     */
    private InventoryDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the database tables
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    /**
     * Upgrades the database tables
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_INVENTORY_TABLE);
        db.execSQL(SQL_DELETE_USER_TABLE);
        onCreate(db);
    }

    /**
     * Retrieves all items from the inventory table
     * @return a list of all the items in the inventory table
     */
    public ArrayList<String> getUsers() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {UserTable.UserEntry.COLUMN_USERNAME};
        ArrayList<String> users = new ArrayList<>();

        try (Cursor cursor = db.query(UserTable.UserEntry.TABLE_NAME, projection, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(UserTable.UserEntry.COLUMN_USERNAME));
                users.add(username);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting users: " + e.getMessage());
        } finally {
            db.close();
        }

        return users;
    }

    /**
     * Validates a user's credentials by checking if their username and hashed password exist in the database.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return true if the user exists and the provided credentials match, false otherwise.
     */
    public boolean validateUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT * FROM " + UserTable.UserEntry.TABLE_NAME + " WHERE username = ? AND password = ?";
        String[] selectionArgs = {username, password};

        try (Cursor cursor = db.rawQuery(sql, selectionArgs)) {
            if (cursor.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error executing SQL query", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }






    String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Get all inventory items
     *
     * @return List of inventory items
     */
    public List<Item> getItems() {
        List<Item> items = new ArrayList<Item>();
        SQLiteDatabase db = getReadableDatabase();

        String sql = "SELECT * FROM " + UserTable.UserEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, new String[]{});
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                int quantity = cursor.getInt(2);
                items.add(new Item(id, name, quantity));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return items;
    }

    /**
     * Add an item to the database
     *
     * @param name     The name of the item
     * @param quantity The quantity of the item
     * @return Whether the item was successfully inserted into the database or not
     */
    public boolean addItem(String name, int quantity) {
        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Set the values according to their columns
        ContentValues values = new ContentValues();
        values.put(InventoryTable.InventoryEntry.COLUMN_NAME, name);
        values.put(InventoryTable.InventoryEntry.COLUMN_QUANTITY, quantity);

        // Insert row
        long itemId = db.insert(InventoryTable.InventoryEntry.TABLE_NAME, null, values);

        return itemId != -1;
    }

    /**
     * Delete an item from the database
     *
     * @param item The item to delete
     * @return Whether the item was successfully deleted or not
     */
    public boolean deleteItem(Item item) {
        // Get a writeable instance of the database
        SQLiteDatabase db = getWritableDatabase();

        // Delete the item matching the given item's ID
        int rowsDeleted = db.delete(InventoryTable.InventoryEntry.TABLE_NAME, InventoryTable.InventoryEntry._ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        // Check that the row was removed from the database
        return rowsDeleted > 0;
    }



    /**
     * Add a new user to the database.
     *
     * @param name     The name of the user.
     * @param password The password of the user.
     * @return True if adding the user to the database was successful, false otherwise.
     */
    public synchronized boolean addUser(String name, String password) {


        // Check if the username already exists in the database
        if (validateUserCredentials(name, password)) {
            return false;
        }

        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();

            // Add the new user to the database
            ContentValues values = new ContentValues();
            values.put(UserTable.UserEntry.COLUMN_USERNAME, name);
            values.put(UserTable.UserEntry.COLUMN_PASSWORD, password);

            long result = db.insert(UserTable.UserEntry.TABLE_NAME, null, values);

            if (result == -1) {
                Log.e(TAG, "Error adding user " + name);
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error adding user " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return false;
    }


    /**
     * Update an existing item
     *
     * @param item The item to update
     * @return Whether the item was successfully updated or not
     */
    public boolean updateItem(Item item) {
        // Get an instance of the writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Set the values according to their columns
        ContentValues values = new ContentValues();
        values.put(InventoryTable.InventoryEntry.COLUMN_NAME, item.getName());
        values.put(InventoryTable.InventoryEntry.COLUMN_QUANTITY, item.getQuantity());

        // Update the item and check that it successfully updated
        int rowsUpdated = db.update(InventoryTable.InventoryEntry.TABLE_NAME, values, InventoryTable.InventoryEntry._ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        return rowsUpdated > 0;
    }


    /**
     * Prints a list of all usernames and their corresponding passwords to the console.
     */
    public void printListOfPasswords() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();

            // Define the columns to retrieve
            String[] projection = {
                    UserTable.UserEntry.COLUMN_USERNAME,
                    UserTable.UserEntry.COLUMN_PASSWORD
            };

            // Query the database
            cursor = db.query(UserTable.UserEntry.TABLE_NAME, projection, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int usernameColumnIndex = cursor.getColumnIndex(UserTable.UserEntry.COLUMN_USERNAME);
                int passwordColumnIndex = cursor.getColumnIndex(UserTable.UserEntry.COLUMN_PASSWORD);
                while (!cursor.isAfterLast()) {
                    String username = cursor.getString(usernameColumnIndex);

                    byte[] passwordBytes = cursor.getBlob(passwordColumnIndex);
                    String password = new String(passwordBytes, StandardCharsets.UTF_8);

                    Log.d(TAG, String.format("Username: %s, Password: %s", username, password));
                    cursor.moveToNext();
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error executing SQL query", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }



    /**
     * Sanitizes input by removing leading/trailing white space and limiting the length of the string.
     *
     * @param input The input string to sanitize.
     * @return The sanitized input string.
     */
    String sanitizeInput(String input) {
        String sanitizedInput = input.trim();
        return sanitizedInput.substring(0, Math.min(sanitizedInput.length(), MAX_INPUT_LENGTH));
    }


    void printListOfUsers() {
        ArrayList<String> users = getUsers();
        Log.d(TAG, "List of users: " + users.toString());
    }


}
