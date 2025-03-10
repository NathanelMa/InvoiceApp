package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import androidx.annotation.NonNull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * DatabaseHelper is a singleton class that handles all database operations for managing invoices,
 * items, and related tables.
 * It manages access to three main tables: itemsTable, invoiceRowsTable, and invoicesFramesTable.
 * This class ensures safe database transactions and offers various operations for
 * manipulating invoice data.
 * NOTE: This class follows a singleton pattern to ensure only one instance of the database
 *       helper is created.
 * NOTE: It is possible for empty invoice, which is: Frame in invoicesFramesTable,
 *       and no matching rows in invoiceRowsTable
 */
final class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "invoices.db";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper INSTANCE;

    private itemsTable itemsTable;
    private invoiceRowsTable invoiceRowsTable;
    private invoicesFramesTable invoicesFramesTable;
    private companyDetailsTable companyDetailsTable;

    /** Private constructor to prevent direct instantiation, and prevent multiple
     * instances from being created. Make call to static method "getInstance()" instead. */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        initializeTables();
    }

    /** Init tables if needed */
    private void initializeTables() {
        if (itemsTable == null) {
            itemsTable = new itemsTable(getWritableDatabase());
        }
        if (invoiceRowsTable == null) {
            invoiceRowsTable = new invoiceRowsTable(getWritableDatabase());
        }
        if (invoicesFramesTable == null) {
            invoicesFramesTable = new invoicesFramesTable(getWritableDatabase());
        }
        if (companyDetailsTable == null) {
            companyDetailsTable = new companyDetailsTable((getWritableDatabase()));
        }
    }

    /**
     * Create an accesses to database and it's operations.
     * @param context Context type, use *application context* and not activity.
     * @return Instance of DatabaseHelper - Will have all operation for database.
     */
    public static DatabaseHelper getInstance(Context context) {
        /*
         * Use the application context as suggested by CommonsWare.
         * this will ensure that we don't accidentally leak (memory) an Activity context.
         */
        if (INSTANCE == null) {
            INSTANCE = new DatabaseHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    /**
     * This method is only run when the database file did not exist and was just created.
     * If onCreate() returns successfully (doesn't throw an exception),
     * the database is assumed to be created with the requested version number.
     * @param database DataBase.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(com.example.invoiceapp.itemsTable.SQL_CREATE_ENTRIES);
        database.execSQL(com.example.invoiceapp.invoiceRowsTable.SQL_CREATE_ENTRIES);
        database.execSQL(com.example.invoiceapp.invoicesFramesTable.SQL_CREATE_ENTRIES);
        database.execSQL(com.example.invoiceapp.companyDetailsTable.SQL_CREATE_ENTRIES);
    }

    /**
     * This method is only called when the database file exists but the stored version number is
     * lower than requested in the constructor. The onUpgrade() should update the table schema
     * to the requested version. In this case will drop all tables.
     * @param database The database.
     * @param i The old database version.
     * @param i1 The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("DROP TABLE IF EXISTS " + com.example.invoiceapp.itemsTable.getTableName());
        database.execSQL("DROP TABLE IF EXISTS " + com.example.invoiceapp.invoiceRowsTable.getTableName());
        database.execSQL("DROP TABLE IF EXISTS " + com.example.invoiceapp.invoicesFramesTable.getTableName());
        database.execSQL("DROP TABLE IF EXISTS " + com.example.invoiceapp.companyDetailsTable.getTableName());
        onCreate(database);
    }

    /*//////////////////////////////////////////////////////////////////////////////////////////////
    Company Table   ////////////////////////////////////////////////////////////////////////////////
    /*//////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Replace the current company profile with a new one.
     * Will erase the previous profile.
     * @param companyDetails New profile for input into the database.
     * @return True if success.
     */
    public boolean UpdateCompany(@NonNull CompanyDetails companyDetails) {
        return this.companyDetailsTable.UpdateCompany(companyDetails);
    }

    public boolean editCompany(@NonNull String columnName,@NonNull String newValue) {
        return this.companyDetailsTable.updateCompanyDetail(columnName, newValue);
    }

    /**
     * Get the current company profile.
     * If no profile found in database, will return null.
     */
    public CompanyDetails getCompany() {
//        SQLiteDatabase db = getWritableDatabase();
//        try {
//            db.execSQL("DROP TABLE IF EXISTS " + com.example.invoiceapp.companyDetailsTable.getTableName());
//            db.execSQL(com.example.invoiceapp.companyDetailsTable.SQL_CREATE_ENTRIES);
//        }
//        catch (Exception e) { }
        return this.companyDetailsTable.getCompany();
    }

    /** Remove current company profile from database */
    public boolean deleteCompany() {
        return this.companyDetailsTable.deleteCompany();
    }

    /*//////////////////////////////////////////////////////////////////////////////////////////////
    ItemTable   ////////////////////////////////////////////////////////////////////////////////////
    /*//////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adding new item to the database. Doesn't provide invalidation item input data.
     * Will check if the item already exist, if so (the field of 'Removed' will be marked as 1),
     * Will return -2. -1 for normal insert error.
     * @param item InvoiceItem. Must contain: Value, Name.
     * @return New item ID as in the data base.
     */
    public int addNewItem(@NonNull Item item) {
        return this.itemsTable.addItem(item);
    }

    /**
     * Edit current item from data base. Doesn't provide invalidation item input data.
     * @param item InvoiceItem. ID same as in database.
     * @return True if successes.
     */
    public boolean editItem(Item item) {
        return this.itemsTable.editItem(item);
    }

    /**
     * Removing the selected (by IDs) item, and return all IDs that has been removed.
     * NOTE: This will mark the item as 'removed': For any case for future use such as display
     * invoice with removed items.
     * @param itemsIDs Selected item by ID to remove from table.
     * @return Set of all invoice IDs items successfully removed from the table. If null,
     *          no success, Or empty Set. If no rows, will return the same items IDs.
     */
    public @NonNull Set<Integer> removeItems(@NonNull Set<Integer> itemsIDs) {
        return this.itemsTable.RemoveItems(itemsIDs);
    }

    /**
     * Get all the list of item found in data base. If no items were found, return empty
     * list. Default, by increasing order (ID). Choose otherwise.
     * @param byName Order data in list, by names.
     * @param byPrice Order data in list, by values.
     * @return List of items, type Item, implement InvoiceItem.
     */
    public @NonNull List<Item> getAllItems(boolean byName, boolean byPrice, boolean showRemoved) {
        return this.itemsTable.getItems(byName, byPrice, showRemoved);
    }

    /*//////////////////////////////////////////////////////////////////////////////////////////////
    invoiceRowsTable ///////////////////////////////////////////////////////////////////////////////
    /*//////////////////////////////////////////////////////////////////////////////////////////////

    public @NonNull List<InvoiceRow> getAllRowsByID(long invoiceID, boolean showRemovedItems) {
        if (invoiceID <= 0) return new ArrayList<>();
        List<Item> allItem = getAllItems(false, false, showRemovedItems);
        return this.invoiceRowsTable.getAllRowsByID(invoiceID, allItem);
    }

    /*//////////////////////////////////////////////////////////////////////////////////////////////
    invoicesFramesTable   //////////////////////////////////////////////////////////////////////////
    /*//////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Return the invoice frame by his UNIQUE ID as found in database
     * @param ID UNIQUE ID invoiceFrame
     * @return Invoice frame. Null if not found.
     */
    public InvoiceFrame getInvoiceById(int ID) {
        return this.invoicesFramesTable.getInvoiceById(ID);
    }

    /**
     * Removing all frame along with their matching rows, given the Set of UNIQUE IDs.
     * If desired only one frame to remove, input into the Set the single ID.
     * Safe deleting: For each removed set of rows in the InvoiceRows, delete the match frame.
     * @param IDs UNIQUE IDs of invoices
     * @return True if all success.
     */
    public boolean RemoveInvoiceFrames(@NonNull Set<Integer> IDs) {
        if (IDs.isEmpty()) return false;
        // Remove rows, return Set is all IDs that has been successfully removed.
        Set<Integer> successIDsRemoved = this.invoiceRowsTable.removeRows(IDs);
        // If Set is not empty, next: Remove all matching invoiceFrame.
        if (!successIDsRemoved.isEmpty())
            return this.invoicesFramesTable.RemoveInvoiceFrames(IDs);
        else return false;
    }

    /**
     * Edit invoice frame - All safe operation.
     * First Removing rows -> Successes? -> edit frame and adding new rows.
     * Safe Edit for safe edit. If any fail, any reason, the old rows will be kept in database.
     * @param ID invoice UNIQUE ID for the invoice frame matching the new rows
     * @param NewInvoiceRows New rows, replace instead of the excited rows.
     * @return True if all safe edited. Any fail, the old rows will be kept in database.
     */
    public boolean editInvoice(int ID, @NonNull List<InvoiceRow> NewInvoiceRows) {
        String fullDateEdit = FormatUtils.getCurrentTimestamp();
        if (ID <= 0 || fullDateEdit.isEmpty()) return false;

        // Remove all rows by given ID. Set contain only one ID (@NoNull)
        // First safe save copy.
        List<InvoiceRow> safeCopy = getAllRowsByID(ID, true);
        Set<Integer> successIDsRemoved = this.invoiceRowsTable.removeRows(Collections.singleton(ID));

        // After removing all rows, edit the selected InvoiceFrame.
        // If successes add the new rows, with the same ID
        // Double check for ID for safe transaction.

        if (!successIDsRemoved.isEmpty() && successIDsRemoved.contains(ID)) {
            double totalPrice = 0;
            for (InvoiceRow r: NewInvoiceRows) totalPrice += r.getTotalRowValue();
            if (this.invoicesFramesTable.EditInvoiceFrame(fullDateEdit, totalPrice, ID))
                return this.invoiceRowsTable.addInvoiceRows(NewInvoiceRows, ID);
            else if (!safeCopy.isEmpty())
                // Fail edit (Not in case of empty invoice - No rows)
                addComposedInvoice(safeCopy);
        }
        return false;
    }

    /**
     * Add composed list of invoice rows to the invoiceRowsTable.
     * Date format "yyyy-MM-dd 00:00:00"
     * @param invoiceRows List type InvoiceRow extend InvoiceItem
     * @return True for success transaction.
     */
    public boolean addComposedInvoice(@NonNull List<InvoiceRow> invoiceRows) {
        String fullDate = FormatUtils.getCurrentTimestamp();
        if (fullDate.isEmpty()) return false;

        try { FormatUtils.formatDateFull(fullDate); }
        catch (ParseException e) { return false; }

        double totalPriceInvoice = 0;
        for (InvoiceRow row: invoiceRows) totalPriceInvoice += row.getTotalRowValue();
        long InvoiceFrameID = this.invoicesFramesTable.addNewInvoiceFrame(fullDate, totalPriceInvoice);

        // Check fail insert Frame
        if (InvoiceFrameID <= 0) return false;

        // Safe insert all rows with the correct InvoiceFrameID as inserted before
        return this.invoiceRowsTable.addInvoiceRows(invoiceRows, InvoiceFrameID);
    }

    /**
     * Get the next invoiceID as shown in the invoicesFramesTable.
     * @return long invoice ID unique
     */
    public long getNextInvoiceID() {
        return this.invoicesFramesTable.getNewInvoiceID();
    }

    /**
     * Get all recent (by date) of all invoice as in the invoicesFramesTable.
     * Each ID matching invoice Row in the invoiceRowsTable.
     * @return List of invoice type InvoiceItem
     */
    public @NonNull List<InvoiceFrame> getByRecentInvoices() {
        return this.invoicesFramesTable.getRecentInvoices();
    }

    /**
     * Search all frame by given date.
     * @param date Search for frame by this date. Format: YYYY-MM-DD.
     * @return List of all frame founded in database. Empty if no found.
     */
    public @NonNull List<InvoiceFrame> getInvoicesByDate(String date) {
        return this.invoicesFramesTable.getInvoicesByDate(date);
    }

    /*//////////////////////////////////////////////////////////////////////////////////////////////
    Statics   //////////////////////////////////////////////////////////////////////////////////////
    /*//////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Will return the best selling item, based on data from the tables.
     * @return Not empty string, contain the name (and price) of best selling item. If the item,
     *  was removed, will contain "(Removed Item)". If no data to patch, will return "NO DATA".
     */
    public @NonNull String bestSellingItem() {
        int itemID = this.invoiceRowsTable.getBestSellingItem();
        Pair<Item, Boolean> resPair = this.itemsTable.getItem(itemID);
        String res = "NO DATA";

        if (resPair != null && resPair.first != null) {
            if (resPair.second) {
                res = "(Removed item) " + resPair.first;
            }
            else res = resPair.first.toString();
        }
        return res;
    }

    /**
     * Calculates the total revenue generated since the beginning of the current month.
     * @return The total revenue as a double value.
     */
    public double getTotalRevenueThisMonth() {
        return this.invoicesFramesTable.getTotalRevenueThisMonth();
    }
}
