package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

/**
 * Interface definition for a callback to be invoked when an item in a
 * RecyclerView is clicked.
 * */
public interface OnItemClickListener {

    /**
     * Called when an item has been clicked.
     * @param position The position of the clicked item in the adapter.
     */
    void onItemClick(int position);
}