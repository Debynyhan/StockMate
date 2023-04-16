package com.zybooks.stockmate;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class InventoryDatabase extends SQLiteOpenHelper {

    // Database name and version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StockMate.db";
    private static final int MAX_INPUT_LENGTH = 10;

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
     * This method checks if a username exists in the database
     * @param username the user to check for
     * @return true if the username exists, false otherwise
     */


    /**
     * This method validate the user by checking if their name password exist
     * in the database
     * @param username The username of the user.
     * @param password The password of the user.
     * @return true if the user exists, false otherwise
     */
    /**
     * This method validates the user by checking if their name and hashed password exist in the database
     * @param username The username of the user.
     * @param password The password of the user.
     * @return true if the user exists, false otherwise
     */
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT * FROM " + UserTable.UserEntry.TABLE_NAME + " WHERE username = ?";
        String[] selectionArgs = {username};

        try (Cursor cursor = db.rawQuery(sql, selectionArgs)) {
            if (cursor.moveToFirst()) {
                int passwordColumnIndex = cursor.getColumnIndex(UserTable.UserEntry.COLUMN_PASSWORD);
                if (passwordColumnIndex >= 0) {
                    String storedHash = cursor.getString(passwordColumnIndex);
                    return MessageDigest.isEqual(hashPassword(password), storedHash.getBytes(StandardCharsets.UTF_8));
                }
            }
            return false;
        } catch (SQLException e) {
            Log.e(TAG, "Error executing SQL query", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    /**
     * Add a new user to the database.
     *
     * @param name     The name of the user.
     * @param password The password of the user.
     * @return True if adding the user to the database was successful, false otherwise.
     */
    public synchronized boolean addUser(String name, String password) {
        // Validate the input
        if (!isValidUsername(name)) {
            return false;
        }
        if (!isValidPassword(password)) {
            return false;
        }

        // Check if the username already exists in the database
        if (usernameExists(name)) {
            return false;
        }

        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();

            // Hash the password before storing it in the database
            String hashedPassword = String.valueOf(hashPassword(password));

            // Add the new user to the database
            ContentValues values = new ContentValues();
            values.put(UserTable.UserEntry.COLUMN_USERNAME, name);
            values.put(UserTable.UserEntry.COLUMN_PASSWORD, password);

            db.insertOrThrow(UserTable.UserEntry.TABLE_NAME, null, values);
            return true;
        } catch (SQLException e) {
            Log.e("Inventory Database", "Error adding user " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return false;
    }


    /**
     * This method add a new item to the inventory database
     * @param name The name of the item
     * @param description The description of the item
     * @param quantity The quantity of the item
     * @return The row ID of the new added item
     */
    public long addItem(String name, String description, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InventoryTable.InventoryEntry.COLUMN_NAME, name);
            values.put(InventoryTable.InventoryEntry.COLUMN_DESCRIPTION, description);
            values.put(InventoryTable.InventoryEntry.COLUMN_QUANTITY, quantity);

            long newRowId = db.insertWithOnConflict(InventoryTable.InventoryEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return newRowId;
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting item into inventory database", e);
            return -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    /**
     * This method updates the item in the inventory database
     * @param item The item to be updated in the database
     * @return true if the update was successful, false otherwise
     */
    public boolean updateItem(Item item) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(InventoryTable.InventoryEntry.COLUMN_NAME, item.getName());
            values.put(InventoryTable.InventoryEntry.COLUMN_QUANTITY, item.getQuantity());

            String selection = InventoryTable.InventoryEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(item.getId()) };

            int rowsUpdated = db.update(InventoryTable.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
            return rowsUpdated > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error executing SQL query", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    /**
     * The method delete items from the inventory database
     * @param itemId The ID of the item to be deleted
     * @return true if the item was deleted successfully, false otherwise
     */
    public boolean deleteItem(long itemId) {
        boolean success = false;
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            // Define the WHERE clause of the SQL statement
            String selection = InventoryTable.InventoryEntry._ID + " = ?";
            String[] selectionArgs = { String.valueOf(itemId) };
            // Delete the item from the table
            int rowsDeleted = db.delete(InventoryTable.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            success = rowsDeleted > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting item with ID " + itemId, e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return success;
    }


    public byte[] hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return hash;
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception as appropriate for your application
            throw new RuntimeException("Error hashing password", e);
        }
    }


    /**
     * Sanitizes input by removing leading/trailing white space and limiting the length of the string.
     *
     * @param input The input string to sanitize.
     * @return The sanitized input string.
     */
    private String sanitizeInput(String input) {
        String sanitizedInput = input.trim();
        return sanitizedInput.substring(0, Math.min(sanitizedInput.length(), MAX_INPUT_LENGTH));
    }

    /**
     * Validates a username to ensure it is not null, empty, or too long.
     *
     * @param username The username to validate.
     * @return True if the username is valid, false otherwise.
     */
    private boolean isValidUsername(String username) {
        String sanitizedUsername = sanitizeInput(username);
        if (sanitizedUsername == null || sanitizedUsername.isEmpty() || sanitizedUsername.length() > MAX_INPUT_LENGTH) {
            return false; // Invalid username
        }
        return true; // Valid username
    }

    /**
     * Validates a password to ensure it is not null, empty, or too long.
     *
     * @param password The password to validate.
     * @return True if the password is valid, false otherwise.
     */
    private boolean isValidPassword(String password) {

        String sanitizedPassword = sanitizeInput(password);

        if (sanitizedPassword == null || sanitizedPassword.isEmpty() || sanitizedPassword.length() > MAX_INPUT_LENGTH) {
            return false; // Invalid password
        }
        return true; // Valid password
    }


    /**
     * Check if the provided username already exists in the database.
     *
     * @param username The username to check.
     * @return True if the username exists, false otherwise.
     */
    private boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + UserTable.UserEntry.TABLE_NAME + " WHERE " +
                UserTable.UserEntry.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        try (Cursor cursor = db.rawQuery(sql, selectionArgs)) {
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count > 0;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error executing SQL query", e);
        } finally {
            db.close();
        }

        return false;
    }


}
