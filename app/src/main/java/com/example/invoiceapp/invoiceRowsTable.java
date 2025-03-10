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
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class invoiceRowsTable {

    private final SQLiteDatabase sqLiteDatabase;

    /** Inner class that defines the table contents */
    private static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "InvoiceRows";
        public static final String INVOICE_ID = "InvoiceID";
        public static final String ITEM_ID = "ItemID";
        public static final String VALUE = "Value";
        public static final String AMOUNT = "Amount";
    }

    /** Query for create the table */
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " +
                    FeedEntry.TABLE_NAME + " ( " +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.INVOICE_ID + " INTEGER NOT NULL," +
                    FeedEntry.ITEM_ID + " INTEGER NOT NULL," +
                    FeedEntry.VALUE +	" REAL NOT NULL," +
                    FeedEntry.AMOUNT + " INTEGER NOT NULL);";

    /**
     * Removing the selected (by invoiceIDs) rows, and return all IDs that has been removed.
     * @param invoiceIDs Selected rows by ID to remove from table.
     * @return Set of all invoice IDs successfully removed from the table. If null, no success,
     *          Or empty Set. If no rows, will return the same invoiceIDs.
     */
    public @NonNull Set<Integer> removeRows(@NonNull Set<Integer> invoiceIDs) {
        if (invoiceIDs.isEmpty()) return new HashSet<>();
        Set<Integer> removedIDs = new HashSet<>();
        boolean success = true;

        // Go over the IDs, if deleted, adding to the HashSet
        sqLiteDatabase.beginTransaction();
        try {
            for (Integer invoiceID : invoiceIDs) {
                String selection = FeedEntry.INVOICE_ID + " = ?";
                String[] selectionArgs = { String.valueOf(invoiceID) };
                try {
                    sqLiteDatabase.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
                    removedIDs.add(invoiceID);
                }
                catch (Exception e) {
                    Log.e(FeedEntry.TABLE_NAME, e.toString());
                    success = false;
                    break;
                }
            }
            if (success) { sqLiteDatabase.setTransactionSuccessful(); }
        } finally { sqLiteDatabase.endTransaction(); }
        return removedIDs;
    }

    public static @NonNull String getTableName() {
        return FeedEntry.TABLE_NAME;
    }

    public invoiceRowsTable(@NonNull SQLiteDatabase db) {
        this.sqLiteDatabase = db;
    }

    /**
     * Add rows into the table, inserted with their InvoiceID
     * @param rows List of InvoiceItem
     * @param invoiceID (long) invoiceID
     * @return True if success
     */
    public boolean addInvoiceRows(List<InvoiceRow> rows, long invoiceID) {
        boolean success = true;
        sqLiteDatabase.beginTransaction();
        try {
            for (InvoiceRow r : rows) {
                ContentValues values = new ContentValues();
                values.put(FeedEntry.INVOICE_ID, invoiceID);
                values.put(FeedEntry.ITEM_ID, r.getItemID());
                values.put(FeedEntry.VALUE, r.getTotalRowValue());
                values.put(FeedEntry.AMOUNT, r.getQuantity());
                long result = sqLiteDatabase.insert(FeedEntry.TABLE_NAME, null, values);
                if (result == -1) success = false;
            }
            if (success) sqLiteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); success = false; }
        finally { sqLiteDatabase.endTransaction(); }
        return success;
    }

    /**
     * Get all invoice rows, given the unique invoice frame ID
     * @param invoiceID Common ID for all requested rows in this table.
     * @return List of InvoiceRow (implements InvoiceItem)
     */
    public @NonNull List<InvoiceRow> getAllRowsByID(long invoiceID, List<Item> itemList) {
        List<InvoiceRow> invoiceList = new ArrayList<>();

        String[] columns = {
                FeedEntry.ITEM_ID,
                FeedEntry.VALUE,
                FeedEntry.AMOUNT
        };

        String selection = FeedEntry.INVOICE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(invoiceID) };

        Cursor cursor = sqLiteDatabase.query(
                FeedEntry.TABLE_NAME,  //
                columns,               //
                selection,             // WHERE
                selectionArgs,         // condition values
                null,                  // groupBy
                null,                  // having
                null                   // orderBy
        );
        try {
            while (cursor.moveToNext()) {
                long itemID = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry.ITEM_ID));
                double totalPriceRow = cursor.getDouble(cursor.getColumnIndexOrThrow(FeedEntry.VALUE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(FeedEntry.AMOUNT));
                double itemPrice = 0.f;
                if (quantity == 0) itemPrice = 0.f;
                else itemPrice = totalPriceRow / quantity;

                Item tempItem = new Item(itemID, "Item Removed", "Item Removed", itemPrice);

                if ((int) itemID - 1 >= itemList.size()) {
                    tempItem = new Item(itemID, "Item Removed", "Item Removed", itemPrice);
                } else {
                    int fixedIndex = (int) itemID - 1;
                    long tempID = itemList.get(fixedIndex).getID();
                    if (tempID == itemID) {
                        tempItem = new Item(
                                itemID,
                                itemList.get(fixedIndex).getName(),
                                itemList.get(fixedIndex).getDescription(),
                                itemPrice);
                    }
                }
                invoiceList.add(new InvoiceRow(invoiceID, quantity, tempItem));
            }
            cursor.close();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return invoiceList;
    }

    /**
     * Retrieves the total revenue from given IDs (Invoice ID).
     * Will go over each row where has the same InvoiceID, and sum it.
     * @param IDs String format "ID_1,ID_2,ID_3,...ID_n"
     * @return Double, total revenue from all selected invoices. Return 0 for any error, or no invoices.
     */
    public double getTotalRevenueByIDs(@NonNull String IDs) {
        if (IDs.isEmpty()) return 0;
        String query =
                "SELECT SUM(" + FeedEntry.VALUE + ") " +
                "FROM " + FeedEntry.TABLE_NAME + " " +
                "WHERE " + FeedEntry.INVOICE_ID + " IN (" + IDs + ");";

        double revenue = 0;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                revenue = cursor.getDouble(0);
                cursor.close();
            }
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return revenue;
    }

    /**
     * Retrieves the ID of the best-selling item, i.e., the item with the highest total quantity sold.
     * @return The ID of the best-selling item, or -1 if no data exists.
     */
    public int getBestSellingItem() {
        // Group by items IDs, (order by) sum each item amount from all rows and patch only 1,
        // which is has highest sum. (Like CSV file 'GROUP BY')
        int bestSellingItem = -1;
        String query =
                "SELECT " + FeedEntry.ITEM_ID + " " +
                        "FROM " + FeedEntry.TABLE_NAME + " " +
                        "GROUP BY " + FeedEntry.ITEM_ID + " " +
                        "ORDER BY SUM(" + FeedEntry.AMOUNT + ") DESC " +
                        "LIMIT 1;";
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                bestSellingItem = cursor.getInt(0);
            }
            cursor.close();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return bestSellingItem;
    }
}
