package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class itemsTable {
    private final SQLiteDatabase database;

    /*
     * This table contain all items with unique ID.
     * Removing operation, will mark the item as removed, but will keep the row in the table,
     * this for any case of using invoice items names / ID as normal.
      */

    /** Inner class that defines the table contents */
    private static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "BasicItem";
        public static final String NAME = "Name";
        public static final String DESCRIPTION = "Description";
        public static final String VALUE = "Value";
        public static final String IS_REMOVED = "isRemoved";
    }

    /** Query for create the table */
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " +
                    FeedEntry.TABLE_NAME + " ( " +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.NAME + " TEXT NOT NULL," +
                    FeedEntry.DESCRIPTION + " TEXT," +
                    FeedEntry.VALUE + " REAL NOT NULL," +
                    FeedEntry.IS_REMOVED + " INTEGER NOT NULL);";

    public static @NonNull String getTableName() {
        return FeedEntry.TABLE_NAME;
    }

    public itemsTable(@NonNull SQLiteDatabase db) {
        this.database = db;
    }

    /**
     * Removing the selected (by IDs) item, and return all IDs that has been removed.
     * NOTE: This will mark the item as 'removed'.
     * @param itemsIDs Selected item by ID to remove from table.
     * @return Set of all invoice IDs items successfully removed from the table. If null,
     *          no success, Or empty Set. If no rows, will return the same items IDs.
     */
    public @NonNull Set<Integer> RemoveItems(@NonNull Set<Integer> itemsIDs) {
        if (itemsIDs.isEmpty()) return new HashSet<>();
        Set<Integer> removedIDs = new HashSet<>();
        boolean success = true;
        // Go over the IDs, if deleted, adding to the HashSet
        database.beginTransaction();
        try {
            for (Integer itemID : itemsIDs) {
                ContentValues values = new ContentValues();
                values.put(FeedEntry.IS_REMOVED, 1);
                String selection = FeedEntry._ID + " = ?";
                String[] selectionArgs = { String.valueOf(itemID) };
                try {
//                    database.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
                    if (database.update(FeedEntry.TABLE_NAME, values, selection, selectionArgs) > 0)
                        removedIDs.add(itemID);
                }
                catch (Exception e) {
                    Log.e(FeedEntry.TABLE_NAME, e.toString());
                    success = false;
                    break;
                }
            }
            if (success) { database.setTransactionSuccessful(); }
        } finally { database.endTransaction(); }
        return removedIDs;
    }

    /**
     * Retrieves an item from the database based on its ID. Pair: first or second can be null.
     * @param ID The unique identifier of the item.
     * @return An Item object if found, otherwise null. If removed, second will be True.
     */
    public Pair<Item, Boolean> getItem(long ID) {
        Item item = null;
        boolean isRemoved = false;
        String selection = FeedEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(ID) };

        Cursor cursor = database.query(
                FeedEntry.TABLE_NAME,
                new String[]{FeedEntry._ID, FeedEntry.NAME, FeedEntry.DESCRIPTION, FeedEntry.VALUE, FeedEntry.IS_REMOVED}, // Columns to retrieve
                selection,      // WHERE clause
                selectionArgs,  // Arguments for WHERE clause
                null,           // GROUP BY
                null,           // HAVING
                null            // ORDER BY
        );

        if (cursor.moveToFirst()) {
            long itemId = cursor.getLong(0);
            String name = cursor.getString(1);
            String description = cursor.getString(2);
            double value = cursor.getDouble(3);
            isRemoved = cursor.getInt(4) == 1;
            item = new Item(itemId, name, description, value);
        }
        cursor.close();
        return new Pair<>(item, isRemoved);
    }

    /**
     * Adding new item to the database. Doesn't provide invalidation item input data.
     * Will check if the item already exist, if so (the field of 'Removed' will be marked as 1),
     * Will return -2. -1 for normal insert database error.
     * @param newItem InvoiceItem. Must contain: Value, Name.
     * @return New item ID as in the data base.
     */
    public int addItem(@NonNull Item newItem) {
        try {
            String selection = FeedEntry.NAME + " LIKE ? AND " + FeedEntry.IS_REMOVED + " = ?";
            String[] selectionArgs = new String[]{newItem.getName().toLowerCase(), "0"};
            try (Cursor cursor = database.query(
                    FeedEntry.TABLE_NAME,
                    new String[] {FeedEntry._ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            )) {
                if (cursor.moveToFirst()) { return -2; }
            }

            ContentValues values = new ContentValues();
            values.put(FeedEntry.DESCRIPTION, newItem.getDescription());
            values.put(FeedEntry.VALUE, newItem.getValue());
            values.put(FeedEntry.NAME, newItem.getName());
            values.put(FeedEntry.IS_REMOVED, 0);

            long tempID = database.insert(FeedEntry.TABLE_NAME, null, values);
            if (tempID == -1) {
                return -1;
            }
            return (int) tempID;
        } catch (Exception e) {
            Log.e("DatabaseError", "Error adding item: " + newItem.getName(), e);
            return -1;
        }
    }

    /**
     * Update item, keep the same ID number for it.
     * @param updatedItem with the same ID as before.
     * @return True if success.
     */
    public boolean editItem(Item updatedItem) {
        if (updatedItem.getID() <= 0 ) return false;

        ContentValues values = new ContentValues();
        values.put(FeedEntry.NAME, updatedItem.getName());
        values.put(FeedEntry.DESCRIPTION, updatedItem.getDescription());
        values.put(FeedEntry.VALUE, updatedItem.getValue());

        /// Update only where the _id matches the itemID
        String selection = FeedEntry._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(updatedItem.getID()) };
        return database.update(
                FeedEntry.TABLE_NAME,   // The table to update
                values,                 // The new values to update
                selection,              // The WHERE clause
                selectionArgs           // The value (_ID) for the WHERE clause
        ) > 0;
    }

    /**
     * Get all items, sorted by name or by price or both.
     * If both false, will return the list of items sorted by ID (last added).
     * @param byName boolean, sorted by names.
     * @param byPrice boolean, sorted by prices.
     * @param includingRemoved Get all items including those removed.
     * @return List (ArrayList) of items. Order as mention
     */
    public @NonNull List<Item> getItems(boolean byName, boolean byPrice, boolean includingRemoved) {
        List<Item> itemsList = new ArrayList<>();
        String orderBy;
        String selection = null;
        String[] selectionArgs = null;

        if (!includingRemoved) {
            selection = FeedEntry.IS_REMOVED + " = ?";
            selectionArgs = new String[]{"0"};
        }

        if (byName && byPrice) orderBy = FeedEntry.NAME + " ASC, " + FeedEntry.VALUE + " ASC";
        else if (byName) orderBy = FeedEntry.NAME + " ASC";
        else if (byPrice) orderBy = FeedEntry.VALUE + " ASC";
        else orderBy = FeedEntry._ID + " ASC";

        // SQL query
        try {
            Cursor cursor = database.query(
                    FeedEntry.TABLE_NAME,
                    new String[]{FeedEntry._ID, FeedEntry.NAME, FeedEntry.DESCRIPTION, FeedEntry.VALUE},
                    selection,
                    selectionArgs,  // FILTER
                    null,
                    null,
                    orderBy
            );
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String description = cursor.getString(2);
                double value = cursor.getDouble(3);
                itemsList.add(new Item(id, name, description, value));
            }
            cursor.close();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return itemsList;
    }
}
