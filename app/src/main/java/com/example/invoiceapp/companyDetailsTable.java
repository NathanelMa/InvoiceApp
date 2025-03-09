package com.example.invoiceapp;

/*
 * @authors     NathanEl Mark, Dor Binyamin, Orel Gigi
 * @date        2025-03-09
 * @copyright   Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * @university  Tel-Hai Academic College
 * @project     This project was developed as part of academic studies at Tel-Hai College.
 *
 * @license     MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

public class companyDetailsTable {
    private final SQLiteDatabase database;

    /** Inner class that defines the table contents */
    private static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "CompanyDetails";
        public static final String COMPANY_NAME = "CompanyName";
        public static final String COMPANY_ADDRESS = "CompanyAddress";
        public static final String COMPANY_NUMBER = "CompanyNumber";
        public static final String COMPANY_ID = "CompanyID";
    }

//    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " +
//            FeedEntry.TABLE_NAME + " ( " +
//                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    FeedEntry.COMPANY_NAME + " TEXT NOT NULL," +
//                    FeedEntry.COMPANY_ADDRESS + " TEXT NOT NULL," +
//                    FeedEntry.COMPANY_NUMBER + " TEXT NOT NULL," +
//                    FeedEntry.COMPANY_ID + " TEXT NOT NULL UNIQUE);";

    /** Query for create the table */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + FeedEntry.TABLE_NAME + " ( " +
                    "ID INTEGER PRIMARY KEY CHECK(ID = 1)," +
                    FeedEntry.COMPANY_NAME + " TEXT NOT NULL UNIQUE," +
                    FeedEntry.COMPANY_ADDRESS + " TEXT NOT NULL UNIQUE," +
                    FeedEntry.COMPANY_NUMBER + " TEXT NOT NULL UNIQUE," +
                    FeedEntry.COMPANY_ID + " TEXT NOT NULL UNIQUE);";

    public static String getTableName() {
        return companyDetailsTable.FeedEntry.TABLE_NAME;
    }

    public companyDetailsTable(@NonNull SQLiteDatabase database) {
        this.database = database;
    }

    public boolean UpdateCompany(CompanyDetails companyDetails) {
        ContentValues values = new ContentValues();
        values.put("ID", 1);
        values.put(FeedEntry.COMPANY_NAME, companyDetails.getCompanyName());
        values.put(FeedEntry.COMPANY_ADDRESS, companyDetails.getCompanyAddress());
        values.put(FeedEntry.COMPANY_NUMBER, companyDetails.getCompanyNumber());
        values.put(FeedEntry.COMPANY_ID, companyDetails.getCompanyId());

        return database.insertWithOnConflict(FeedEntry.TABLE_NAME, null,
                values, SQLiteDatabase.CONFLICT_REPLACE) > 0;
    }

    /**
     * Get the current company profile.
     * If no profile found in database, will return null.
     */
    public CompanyDetails getCompany() {
        CompanyDetails cd = null;
        Cursor cursor = database.query(
                FeedEntry.TABLE_NAME,
                null,
                "ID = 1",
                null,
                null,
                null,
                null
        );
        try {
            if (cursor.moveToFirst()) {
                String companyName = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COMPANY_NAME));
                String companyAddress = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COMPANY_ADDRESS));
                String companyNumber = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COMPANY_NUMBER));
                String companyId = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COMPANY_ID));
                cd = new CompanyDetails(companyName, companyAddress, companyNumber, companyId);
                cursor.close();
            }
            cursor.close();
        }
        catch (Exception e) { Log.e(FeedEntry.TABLE_NAME, e.toString()); }
        return cd;
    }

    public boolean updateCompanyDetail(String columnName, String newValue) {
        ContentValues values = new ContentValues();
        values.put(columnName, newValue);
        return database.update(FeedEntry.TABLE_NAME, values, "ID = 1", null) > 0;
    }

    public boolean deleteCompany() {
        return database.delete(FeedEntry.TABLE_NAME, "ID = 1", null) > 0;
    }
}

//    /** Query for create the table */
//    public static final String SQL_CREATE_ENTRIES =
//            "CREATE TABLE IF NOT EXISTS " + FeedEntry.TABLE_NAME + " ( " +
//                    "ID INTEGER PRIMARY KEY CHECK(ID = 1)," +
//                    FeedEntry.COMPANY_NAME + " TEXT NOT NULL UNIQUE," +
//                    FeedEntry.COMPANY_ADDRESS + " TEXT NOT NULL UNIQUE," +
//                    FeedEntry.COMPANY_NUMBER + " TEXT NOT NULL UNIQUE," +
//                    FeedEntry.COMPANY_ID + " TEXT NOT NULL UNIQUE);";