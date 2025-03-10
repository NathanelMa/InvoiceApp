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
import java.util.List;
import java.util.Set;

public class invoicesFramesTable {

    /**
     * All ID are Unique, but, it can be recycled after been removed.
     */

    private final SQLiteDatabase sqLiteDatabase;

    /** Inner class that defines the table contents */
    private static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "InvoiceFrame";
        public static final String DATE = "Date";
        public static final String PRICE = "Price";
    }

    /** Query for create the table */
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " +
                        FeedEntry.TABLE_NAME + " ( " +
                        FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        FeedEntry.DATE + " TEXT NOT NULL," +
                        FeedEntry.PRICE + " REAL NOT NULL);";

    public static String getTableName() {
        return FeedEntry.TABLE_NAME;
    }

    /**
     * Edit invoice frame by given 'editID'. New values: date and invoiceValue.
     * Safe edit.
     * @param dateFormatted Date must be format as "yyyy-MM-dd hh:mm:ss".
     * @param invoiceValue Total price of all rows in the invoice.
     * @param editID InvoiceID as in database.
     * @return Edit successfully.
     */
    public boolean EditInvoiceFrame(@NonNull String dateFormatted, double invoiceValue, int editID) {
        if (dateFormatted.isEmpty() || editID <= 0) return false;
        boolean success = false;
        String selection = FeedEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(editID) };
        sqLiteDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.DATE, dateFormatted);
            values.put(FeedEntry.PRICE, invoiceValue);
            int rowsAffected = sqLiteDatabase.update(FeedEntry.TABLE_NAME, values, selection, selectionArgs);
            if (rowsAffected > 0) {
                success = true;
                sqLiteDatabase.setTransactionSuccessful();
            }
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        finally { sqLiteDatabase.endTransaction(); }
        return success;
    }

    public invoicesFramesTable(SQLiteDatabase db) {
        this.sqLiteDatabase = db;
    }

    /**
     * Removing all selected invoices by given UNIQUE ID of the frames.
     * @param IDs Set, UNIQUE IDs of the invoices
     * @return True if successes.
     */
    public boolean RemoveInvoiceFrames(Set<Integer> IDs) {
        int deletedRows = 0;
        sqLiteDatabase.beginTransaction();
        try {
            for (Integer ID: IDs) {
                String selection = FeedEntry._ID + " = ?";
                String[] selectionArgs = { String.valueOf(ID) };
                deletedRows += sqLiteDatabase.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
            }
            sqLiteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            { Log.e(FeedEntry.TABLE_NAME, e.toString()); }

            return false;
        }
        finally { sqLiteDatabase.endTransaction(); }
        return deletedRows > 0;
    }

    /**
     * Get the next (if will be created new row) invoice ID as in database.
     * If in the past were invoices and now the table is empty, will return 1.
     * First invoice ID is 1 as default defined in sqlite.
     * @return long next invoice ID
     */
    public long getNewInvoiceID() {
        String sqliteSequenceTableName = "sqlite_sequence";
        String[] columns = {"seq"};
        String selection = "name=?";
        long autoIncrement = 0;
        try {
            String[] selectionArgs = {FeedEntry.TABLE_NAME};
            Cursor cursor = sqLiteDatabase.query(sqliteSequenceTableName,
                    columns, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                int indexSeq = cursor.getColumnIndex(columns[0]);
                autoIncrement = cursor.getLong(indexSeq);
            }
            cursor.close();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return autoIncrement + 1;
    }

    /**
     * Get all recent invoice by date (last inserted).
     * @return List of invoiceItem type InvoiceFrame
     */
    public @NonNull List<InvoiceFrame> getRecentInvoices() {
        List<InvoiceFrame> invoicesList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query =
                    "SELECT " + FeedEntry._ID + ", " + FeedEntry.DATE + ", " + FeedEntry.PRICE +
                            " FROM " + FeedEntry.TABLE_NAME +
                            " ORDER BY " + FeedEntry.DATE + " DESC";

            cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    long ID = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.DATE));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(FeedEntry.PRICE));
                    invoicesList.add(new InvoiceFrame(ID, date, price));
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        finally { if (cursor != null) cursor.close(); }
        return invoicesList;
    }

    /**
     * Add new invoice frame to the data base and return the UNIQUE ID as shown in
     * this table. -1 If not success.
     * @param dateFormated Date must be format as "yyyy-MM-dd hh:mm:ss".
     * @param invoiceValue Total price of all rows in the invoice.
     * @return ID of the invoice frame. -1 If not successes.
     */
    public long addNewInvoiceFrame(String dateFormated, double invoiceValue) {
        boolean success = true;
        long ID = 0;
        sqLiteDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.DATE, dateFormated);
            values.put(FeedEntry.PRICE, invoiceValue);
            ID = sqLiteDatabase.insert(FeedEntry.TABLE_NAME, null, values);
            if (ID == -1) success = false;
            if (success) sqLiteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, "Error updating invoice", e);  }
        finally { sqLiteDatabase.endTransaction(); }
        return ID;
    }

    /**
     * Get all invoice frames, by date match.
     * @param date Date lookup. Format: YYYY-MM-DD
     * @return (@NoNull) List of invoice frames, matching the same date.
     */
    public @NonNull List<InvoiceFrame> getInvoicesByDate(String date) {
        List<InvoiceFrame> invoices = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE " + FeedEntry.DATE + " LIKE ?";
            cursor = sqLiteDatabase.rawQuery(query, new String[]{date + "%"});
            while (cursor.moveToNext()) {
                invoices.add(new InvoiceFrame(
                        cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.DATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(FeedEntry.PRICE))
                ));
            }
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        finally { if (cursor != null) cursor.close(); }
        return invoices;
    }

    /**
     * Get all invoice frame found in the database.
     * @return List of all frames.
     */
    public List<InvoiceFrame> getAllInvoices() {
        List<InvoiceFrame> invoices = new ArrayList<>();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + FeedEntry.TABLE_NAME;
            cursor = sqLiteDatabase.rawQuery(query, null);
            while (cursor.moveToNext()) {
                invoices.add(new InvoiceFrame(
                        cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.DATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(FeedEntry.PRICE))
                ));
            }
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        finally { if (cursor != null) cursor.close(); }
        return invoices;
    }

    /**
     * Find invoice by UNIQUE ID as in table.
     * @param ID UNIQUE ID invoice target frame
     * @return InvoiceFrame. Null if not found.
     */
    public InvoiceFrame getInvoiceById(int ID) {
        InvoiceFrame invoice = null;
        Cursor cursor = null;
        try {
            cursor = sqLiteDatabase.rawQuery(
                    "SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE " + FeedEntry._ID
                            + " = ?", new String[]{ String.valueOf(ID) });
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(FeedEntry._ID);
                int dateIndex =cursor.getColumnIndexOrThrow(FeedEntry.DATE);
                int priceIndex = cursor.getColumnIndexOrThrow(FeedEntry.PRICE);

                if (idIndex != -1 && dateIndex != -1 && priceIndex != -1) {
                    invoice = new InvoiceFrame(
                            cursor.getInt(idIndex),
                            cursor.getString(dateIndex),
                            cursor.getDouble(priceIndex)
                    );
                }
            }
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        finally { if (cursor != null) cursor.close(); }
        return invoice;
    }

    /**
     * Retrieves the total revenue from the beginning of the current month.
     * @return The total revenue for the current month.
     */
    public double getTotalRevenueThisMonth() {
        String query =
                "SELECT SUM(" + FeedEntry.PRICE + ") " +
                        "FROM " + FeedEntry.TABLE_NAME + " " +
                        "WHERE strftime('%Y-%m', " + FeedEntry.DATE + ") = strftime('%Y-%m', 'now');";
        double revenue = 0;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                revenue = cursor.getDouble(0);
            }
            cursor.close();

        } catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return revenue;
    }

    /**
     * Retrieves the date with the highest total revenue.
     * @return The date with the highest revenue as a string, or null if no data exists.
     */
    public String getHighestRevenueDate() {
        String query =
                "SELECT " + FeedEntry.DATE + " " +
                        "FROM " + FeedEntry.TABLE_NAME + " " +
                        "ORDER BY " + FeedEntry.PRICE + " DESC " +
                        "LIMIT 1;";

        String highestRevenueDate = null;
        try {
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst())
                highestRevenueDate = cursor.getString(0);
            cursor.close();
        } catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return highestRevenueDate;
    }
}