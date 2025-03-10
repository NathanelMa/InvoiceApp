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
 * Represents a single row in an invoice, containing an item, its quantity, and the calculated total price for that row.
 * This class also handles updating quantities and calculating the total price based on the quantity of the item.
 */
public class InvoiceRow implements Comparable<InvoiceRow> {

    private final long invoiceID;
    private final Item item;
    private int quantity;
    private double totalPrice;

    /**
     * Constructs an InvoiceRow with the given invoice ID, quantity, and item.
     * This constructor does not perform any validation on the data.
     * @param invoiceID the ID of the invoice that this row belongs to.
     * @param quantity  the quantity of the item in this invoice row.
     * @param rowItem   the item that is being invoiced.
     */
    public InvoiceRow(long invoiceID, int quantity, @NonNull Item rowItem) {
        this.quantity = quantity;
        this.invoiceID = invoiceID;
        this.item = rowItem;
        updateTotalPrice();
    }

    /**
     * Gets the ID of the item associated with this invoice row.
     * @return the ID of the item.
     */
    public long getItemID() {
        return this.item.getID();
    }

    /**
     * Gets the name of the item associated with this invoice row.
     * @return the name of the item.
     */
    public String getItemName() {
        return item.getName();
    }

    /**
     * Gets the description of the item associated with this invoice row.
     * @return the description of the item.
     */
    public String getItemDescription() {
        return this.item.getDescription();
    }

    /**
     * Gets the value (price) of the item associated with this invoice row.
     * @return the value of the item.
     */
    public double getItemValue() {
        return this.item.getValue();
    }

    /**
     * Gets the total value of this invoice row (item value * quantity).
     * @return the total value of the row.
     */
    public double getTotalRowValue() {
        return this.totalPrice;
    }

    /**
     * Gets the invoice ID associated with this row.
     * @return the invoice ID.
     */
    public long getInvoiceID() {
        return this.invoiceID;
    }

    /**
     * Gets the quantity of the item in this invoice row.
     * @return the quantity of the item.
     */
    public int getQuantity() {
        return this.quantity;
    }

    /**
     * Checks if the invoice row is considered empty (quantity <= 0).
     * @return true if the row is empty (quantity <= 0), false otherwise.
     */
    public boolean isEmpty() {
        return getQuantity() <= 0;
    }

    /**
     * Updates the total price for this invoice row based on the current quantity and item value.
     */
    private void updateTotalPrice() {
        this.totalPrice = this.item.getValue() * this.quantity;
    }

    /**
     * Increases the quantity of the item in this row by a specified amount and updates the total price.
     * @param add the amount to increase the quantity by.
     */
    public void addQuantity(int add) {
        this.quantity += add;
        updateTotalPrice();
    }

    /**
     * Decreases the quantity of the item in this row by 1, if the quantity is greater than 0, and updates the total price.
     */
    public void reduceQuantity() {
        if (quantity > 0) {
            this.quantity--;
            updateTotalPrice();
        }
    }

    /**
     * Returns a string representation of the invoice row, including the invoice ID, item ID, item name, quantity, and total price.
     *
     * @return a string representation of the invoice row.
     */
    @NonNull
    @Override
    public String toString() {
        return "#" + getInvoiceID() + " #" + getItemID() + " " + getItemName() + " " + getQuantity() + " " + getTotalRowValue();
    }

    /**
     * Compares this InvoiceRow with another InvoiceRow based on the item ID.
     * @param other the other InvoiceRow to compare with.
     * @return a negative integer, zero, or a positive integer if this row's item ID is less than, equal to, or greater than the other row's item ID.
     */
    public int compareTo(InvoiceRow other) {
        return Long.compare(getItemID(), other.getItemID());
    }

    /**
     * Checks if this InvoiceRow is equal to another object.
     * Two InvoiceRows are considered equal if they have the same item ID.
     * @param o the object to compare to.
     * @return true if this InvoiceRow is equal to the other object, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceRow other = (InvoiceRow) o;
        return this.getItemID() == other.getItemID();
    }

    /**
     * Returns a hash code for this InvoiceRow, based on the item ID.
     * @return a hash code for this InvoiceRow.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.item.getID());
    }
}
