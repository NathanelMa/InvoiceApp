package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Represents an invoice frame.
 * Each invoice has final unique ID as stored in database.
 * Each new ID can't be recycled by database. Date format "yyyy-MM-dd hh:mm:ss".
 */
public class InvoiceFrame implements Comparable<InvoiceFrame> {
    private final String date;
    private final double totalValue;
    private final long ID;

    /**
     * Compose invoice frame including unique ID, date and value.
     * @param ID Unique ID
     * @param date Formatted: yyyy-mm-dd hh:mm:ss
     * @param totalValue Double for total value for this invoice
     */
    public InvoiceFrame(long ID, @NonNull String date, double totalValue) {
        this.date = date;
        this.totalValue = totalValue;
        this.ID = ID;
    }

    /**
     * Get invoice frame created date, format: "yyyy-MM-dd hh:mm:ss"
     * @return String, formated date
     */
    public String getDate() {
        return this.date;
    }

    /**
     * @return Get total price for this invoice
     */
    public double getTotalPrice() {
        return this.totalValue;
    }

    /**
     * @return Get the unique invoice ID as in database
     */
    public long getID() {
        return this.ID;
    }

    public boolean isEmpty() {
        return date.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return  getID() + " " + getDate() + " " + getTotalPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceFrame)) return false;
        InvoiceFrame that = (InvoiceFrame) o;
        return ID == that.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ID);
    }

    /**
     * Compare only by ID. No recycling for invoices ID's.
     * Each ID number is new (highest, the newest)
     * @param invoiceFrame Frame of invoice to compare to.
     * @return If the given frame is newest.
     */
    @Override
    public int compareTo(@NonNull InvoiceFrame invoiceFrame) {
        return Long.compare(this.getID(), invoiceFrame.getID());
    }

    /** Comparator by invoice date dd-MM-yyyy format. Comparing only day, month and year */
    public static int compareToByDate(@NonNull InvoiceFrame f1, @NonNull InvoiceFrame f2) {
        int res;
        res = f2.getDate().compareTo(f1.getDate());
        return res;
    }

    /** Comparator by invoice values */
    public static int compareToByValue(InvoiceFrame f1, InvoiceFrame f2) {
        return Double.compare(f2.getTotalPrice(), f1.getTotalPrice());
    }

    /** Comparator by invoice dates (day, month, year) recent is first. Then, comparing if
     * The same day, compering by value: Highest first. */
    public static int compareToDateValue(@NonNull InvoiceFrame f1,@NonNull InvoiceFrame f2) {
        int res;
        try {
            res = compareToByDate(f1,f2);
            if (res == 0) res = compareToByValue(f1, f2);
        }
        catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }

    /** Comparator by price (Highest first), then compare by invoice dates (day, month, year) recent is first. */
    public static int compareToValueDate(@NonNull InvoiceFrame f1,@NonNull InvoiceFrame f2) {
        int res;
        try {
            res = compareToByValue(f1, f2);
            if (res == 0) res = compareToByDate(f1,f2);
        }
        catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }
}
