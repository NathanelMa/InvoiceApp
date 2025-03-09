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

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Adapter for displaying InvoiceItem in a RecyclerView.
 * This adapter binds a list of items to views that are displayed within a RecyclerView.
 * Also, will handle selected InvoiceItem by user. Use method getSelectedItems().
 */

public class RVAdapterItems extends
        ListAdapter<Item, RVAdapterItems.ViewHolder> {

    private final Context context;
    private final Set<Integer> localSelection; // Buffer selection by user. Contain positions.

    /**
     * Initialize the dataset of the Adapter.
     * @param context The application context.
     */
    public RVAdapterItems(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.localSelection = new TreeSet<>();
        this.setHasStableIds(true);
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).
                inflate(R.layout.item_recycler_view, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called when a view created by this adapter has been recycled.
     * Check if it been selected before, and remove it from set.
     * @param holder The ViewHolder for the view being recycled
     */
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (this.localSelection.isEmpty()) {
            ((CardView) holder.itemView).setCardBackgroundColor(
                    context.getResources().getColor(R.color.white));
        }
    }

    /**
     * Return the stable ID for the item at <code>position</code>.
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        Item item = getItem(position);
        return (item != null) ? item.getID() : RecyclerView.NO_ID;
    }

    /**
     * Replace the contents of a view (invoked by the layout manager).
     * @param holder The ViewHolder instance.
     * @param position The position of the Item within the item list.
     */
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Item item = getItem(position);
        if (item == null) return;

        holder.item_name.setText(item.getName());
        holder.item_description.setText(item.getDescription());
        holder.item_id.setText(FormatUtils.formatSerialNumber(item.getID()));
        holder.item_value.setText(FormatUtils.formatCurrency(item.getValue()));

        // Handle selection
        holder.itemView.setOnClickListener(v -> toggleSelection(holder));
        if (localSelection.contains(position))
            ((CardView) holder.itemView).setCardBackgroundColor(
                    context.getResources().getColor(R.color.Cornsilk));
        else
            ((CardView) holder.itemView).setCardBackgroundColor(
                    context.getResources().getColor(R.color.White));
    }

    /** Will clear the selection list, as well the background */
    public void clearSelection() {
        for (int index: localSelection) notifyItemChanged(index);
        this.localSelection.clear();
    }

    /**
     * Return if the one or more selected item (marked)
     * @return True if empty.
     */
    public boolean isSelectedEmpty() {
        return this.localSelection.isEmpty();
    }

    /**
     * If selection list contain one item, it will return it, else will return null.
     */
    public Item getOneSelectedItem() {
        if (this.localSelection.size() == 1) {
            Iterator<Integer> iterator = this.localSelection.iterator();
            int pos = iterator.next();
            return this.getItem(pos);
        }
        else return null;
    }

    /**
     * Return the selected items (type Item).
     * @return List of all selected items
     */
    public @NonNull List<Item> getSelectedItems() {
        List<Item> itemsList = new ArrayList<>();
        for (Integer pos: this.localSelection) itemsList.add(getItem(pos));
        return itemsList;
    }

    /**
     * Return the IDs of all selected items.
     * @return Set of all items IDs.
     */
    public @NonNull Set<Integer> getSelectedIDs() {
        Set<Integer> IDs = new TreeSet<>();
        List<Item> selectedItems = this.getSelectedItems();
        for (Item itemRemoving : selectedItems)
            IDs.add((int) (itemRemoving.getID()));
        return IDs;
    }

    private void toggleSelection(ViewHolder holder) {
        boolean wasSelected = localSelection.contains(holder.getAdapterPosition());
        if (wasSelected) {
            // holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.White));
            ((CardView) holder.itemView).setCardBackgroundColor(
                    context.getResources().getColor(R.color.White));
            localSelection.remove(holder.getAdapterPosition());
        }
        else {
            localSelection.add(holder.getAdapterPosition());
            ((CardView) holder.itemView).setCardBackgroundColor(
                    context.getResources().getColor(R.color.Cornsilk));
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.getID() == newItem.getID();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
            return oldItem.equals(newItem);
        }
        @Override
        public Object getChangePayload(@NonNull Item oldItem, @NonNull Item newItem) {
            return super.getChangePayload(oldItem, newItem);
        }
    };

    /** ViewHolder class for caching view references */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_name;
        private final TextView item_id;
        private final TextView item_description;
        private final TextView item_value;

        /**
         * Constructor that initializes the UI components for each Item.
         * @param itemView The root view of the Item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.item_name = itemView.findViewById(R.id.RV_item_name);
            this.item_id = itemView.findViewById(R.id.RV_item_ID);
            this.item_description = itemView.findViewById(R.id.RV_item_description);
            this.item_value = itemView.findViewById(R.id.RV_item_value);
        }
    }
}
