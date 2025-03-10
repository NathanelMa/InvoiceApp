package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a shopping cart (ShoppingCard) for an invoice, which contains a list of items (InvoiceRows).
 * It calculates and manages the total price and total number of items in the cart.
 */
public class ShoppingCard {

    private final Set<InvoiceRow> composedList;
    private final long invoiceID;
    private double totalPrice;
    private int totalItems;

    /**
     * Constructor for initializing the ShoppingCard object.
     * @param editedList List of edited invoice rows to initialize the card with. If null or empty, an empty cart will be created.
     * @param invoiceID The ID of the invoice associated with this cart.
     */
    public ShoppingCard(List<InvoiceRow> editedList, long invoiceID) {
        this.invoiceID = invoiceID;
        this.totalItems = 0;
        this.totalPrice = 0;
        // If for edit
        if (editedList != null && !editedList.isEmpty()) {
            this.composedList = new LinkedHashSet<>(editedList);
            for (InvoiceRow row: this.composedList) {
                totalItems++;
                totalPrice += row.getTotalRowValue();
            }
        }
        else {
            this.composedList = new LinkedHashSet<>();
        }
    }

    /**
     * Gets the size of the composed list (number of items in the cart).
     * @return The size of the composed list.
     */
    public int getComposedListSize() {
        return this.composedList.size();
    }

    /**
     * Gets the composed list of invoice rows.
     * @return A list containing all items in the cart.
     */
    public List<InvoiceRow> getComposedList() {
        return new ArrayList<>(composedList);
    }

    /**
     * Gets the invoice ID associated with this cart.
     * @return The invoice ID.
     */
    public long getInvoiceID() {
        return invoiceID;
    }

    /**
     * Gets the total price of all items in the cart.
     * @return The total price of the items in the cart.
     */
    public double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Gets the total number of items in the cart.
     * @return The total number of items in the cart.
     */
    public int getTotalItems() {
        return totalItems;
    }

    /**
     * Removes the selected items from the cart based on their indexes.
     * @param selectedIndexes A set of indexes representing the items to remove from the cart.
     */
    public void removeSelectedItems(@NonNull Set<Integer> selectedIndexes) {
        if (selectedIndexes.isEmpty()) return;

        List<Integer> sortedIndexes = new ArrayList<>(selectedIndexes);
        Collections.sort(sortedIndexes);

        Iterator<InvoiceRow> iterator = this.composedList.iterator();
        int currentIndex = 0;
        int nextRemoveIndex = sortedIndexes.get(0);
        int removeIndexPosition = 0;

        while (iterator.hasNext()) {
            InvoiceRow tempRow = iterator.next();
            if (currentIndex == nextRemoveIndex) {
                this.totalPrice -= tempRow.getTotalRowValue();
                this.totalItems--;
                iterator.remove();
                removeIndexPosition++;
                if (removeIndexPosition < sortedIndexes.size()) {
                    nextRemoveIndex = sortedIndexes.get(removeIndexPosition);
                }
                else break;
            }
            currentIndex++;
        }
    }

    /**
     * Adds a new row to the cart. If the item already exists in the cart, it updates its quantity.
     *
     * @param newRow The invoice row to be added to the cart.
     */
    public void addRow(@NonNull InvoiceRow newRow) {
        // If the row existed in composed list
        int insertedIndex = 0;
        if (this.composedList.contains(newRow))
            for (InvoiceRow row : this.composedList) {
                if (row.equals(newRow)) {
                    this.totalPrice += newRow.getTotalRowValue();
                    row.addQuantity(newRow.getQuantity());
                    break;
                }
                else insertedIndex++;
            }
            // Else add new row
        else {
            insertedIndex = this.composedList.size();
            this.composedList.add(newRow);
            this.totalItems++;
            this.totalPrice += newRow.getTotalRowValue();
        }
    }

    /**
     * Reduces the quantity of selected items. If an item's quantity reaches zero, it is removed from the cart.
     * @param selectedIndexes A list of indexes representing the items to reduce in quantity.
     * @return A boolean indicating if any item was removed due to quantity reduction.
     */
    public boolean reduceSelectedItemQuantity(@NonNull List<Integer> selectedIndexes) {
        int index = 0;
        int selectedIndex = 0;
        boolean anyRemoved = false;
        for (InvoiceRow row : this.composedList) {
            if (selectedIndex < selectedIndexes.size() && index == selectedIndexes.get(selectedIndex)) {
                row.reduceQuantity();
                this.totalPrice += -1 * row.getItemValue();
                selectedIndex++;
            }
            index++;
        }

        Iterator<InvoiceRow> iterator = this.composedList.iterator();
        while (iterator.hasNext()) {
            InvoiceRow checkRow = iterator.next();
            if (checkRow.getQuantity() == 0) {
                this.totalItems--;
                iterator.remove();
                anyRemoved = true;
            }
        }
        return anyRemoved;
    }

    public void addSelectedItemsQuantity(@NonNull List<Integer> selectedIndexes) {
        int index = 0;
        int selectedIndex = 0;
        for (InvoiceRow row : this.composedList) {
            if (selectedIndex < selectedIndexes.size() && index == selectedIndexes.get(selectedIndex)) {
                row.addQuantity(1);
                this.totalPrice += row.getItemValue();
                selectedIndex++;
            }
            index++;
        }
    }
}

