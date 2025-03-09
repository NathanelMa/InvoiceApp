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

import androidx.annotation.NonNull;
import java.util.Objects;

public class Item implements Comparable<Item> {

    /**
     * Represents an item in an invoice, containing an ID, name, description, and value.
     * This class provides methods for item data validation, comparison, and management.
     */

    private final long ID;  // Unique ID for the item in the database
    private final String name;  // Name of the item, as stored in the database
    private final String description; // Description of the item, as stored in the database
    private final double value; // Value (price) of the item, as stored in the database
    public static final int ITEM_NAME_MAX_LEN = 50;
    public static final int ITEM_DESCRIPTION_MAX_LEN = 50;

    /**
     * Constructor for creating an item with the provided ID, name, description, and value.
     * No validation for the data is performed in this constructor.
     * @param ID Unique identifier for the item.
     * @param name Name of the item. Should be less than ITEM_NAME_MAX_LEN characters.
     * @param description Description of the item. Should be less than ITEM_DESCRIPTION_MAX_LEN characters.
     * @param value Value (price) of the item, must be greater than 0.
     */
    public Item(long ID, @NonNull String name, String description, double value) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.ID = ID;
    }

    /**
     * @return the unique ID of the item.
     */
    public long getID() {
        return this.ID;
    }

    /**
     * @return the name of the item.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the description of the item.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the value (price) of the item.
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Checks if the item has an empty name.
     * @return true if the name is empty; otherwise false.
     */
    public boolean isEmpty() {
        return this.getName().isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + ", " + FormatUtils.formatCurrency(getValue());
    }

    /**
     * Validates the data for an item, checking name, description, and value.
     * If valid, returns a new Item instance. If invalid, returns an error message.
     * @param name Name of the item.
     * @param description Description of the item.
     * @param value Value (price) of the item.
     * @return either a new Item instance or an error message.
     */
    public static Object checkValidData(String name, String description, String value) {
        return checkValidData(name, description, value, 0);
    }

    /**
     * Validates the data for an item, checking name, description, and value.
     * If valid, returns a new Item instance with the provided ID. If invalid, returns an error message.
     * @param name Name of the item.
     * @param description Description of the item.
     * @param value Value (price) of the item.
     * @param ID ID for the item.
     * @return either a new Item instance or an error message.
     */
    public static Object checkValidData(String name, String description, String value, long ID) {
        final String INVALID_ITEM_NAME = "Invalid item name";
        final String INVALID_ITEM_NAME_LEN = "Invalid item name length";
        final String INVALID_ITEM_DESCRIPTION = "Invalid item description";
        final String INVALID_ITEM_DESCRIPTION_LEN = "Invalid item description length";
        final String INVALID_ITEM_VALUE = "Invalid item value";

        if (name == null || name.trim().isEmpty() || !name.matches("^[a-zA-Z0-9 ]+$"))
            return INVALID_ITEM_NAME;
        if (name.length() > ITEM_NAME_MAX_LEN) return INVALID_ITEM_NAME_LEN;

        if (description == null
                || description.trim().isEmpty()
                || !description.matches("^[a-zA-Z0-9,. ]+$"))
            return INVALID_ITEM_DESCRIPTION;
        if (description.length() > ITEM_DESCRIPTION_MAX_LEN) return INVALID_ITEM_DESCRIPTION_LEN;

        double price;
        try {
            price = Double.parseDouble(value);
            if (price <= 0) {
                return INVALID_ITEM_VALUE; // Must be positive
            }
        } catch (NumberFormatException e) {
            return INVALID_ITEM_VALUE;
        }
        return new Item(ID, name, description, price);
    }

    /**
     * Compares this item with another item based on their names.
     * @param invoiceItem The item to compare to.
     * @return a negative integer, zero, or a positive integer as this item's name is less than,
     *         equal to, or greater than the specified item's name.
     */
    @Override
    public int compareTo(Item invoiceItem) {
        return this.name.compareToIgnoreCase(invoiceItem.getName());
    }

    /**
     * Checks if this item is equal to another object.
     * Two items are considered equal if they have the same ID.
     * @param o The object to compare to.
     * @return true if the objects are the same; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return getID() == item.getID() &&
                getValue() == item.getValue() &&
                getName().equals(item.getName()) &&
                getDescription().equals(item.getDescription());
    }

    /**
     * Returns a hash code value for the item.
     * @return the hash code value for the item.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(getID());
    }

    /** Comparator by values */
    public static int compareToByValue(Item f1, Item f2) {
        return Double.compare(f2.getValue(), f1.getValue());
    }

    /** Comparator by names */
    public static int compareToByName(Item f1, Item f2) {
        return f1.getName().compareTo(f2.getName());
    }

    /** Comparator by value first, then by name. */
    public static int compareToValueName(@NonNull Item f1,@NonNull Item f2) {
        int res;
        try {
            res = compareToByName(f1, f2);
            if (res == 0) res = compareToByValue(f1,f2);
        }
        catch (Exception e) { throw new RuntimeException(e); }
        return res;
    }
}
