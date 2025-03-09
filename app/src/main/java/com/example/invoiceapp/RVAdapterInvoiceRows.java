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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/** RecyclerView Adapter */
public class RVAdapterInvoiceRows extends ListAdapter<InvoiceRow, RVAdapterInvoiceRows.ViewHolder> {

    private final Context context;
    private final Set<Integer> localSelection;

    public RVAdapterInvoiceRows(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.localSelection = new TreeSet<>();
        this.setHasStableIds(true);
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
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
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceRow invoiceRow = getItem(position);
        if (invoiceRow == null) return;

        // Format the data
        String itemID = FormatUtils.formatSerialNumber(invoiceRow.getItemID());
        String itemName = invoiceRow.getItemName() + ", "
                + FormatUtils.formatCurrency(invoiceRow.getItemValue());
        String itemDescription = invoiceRow.getItemDescription();
        String itemQuantity = "Quantity: " + FormatUtils.formatInteger(invoiceRow.getQuantity());
        String totalPriceRow = FormatUtils.formatCurrency(invoiceRow.getTotalRowValue());

        // Set text values
        holder.item_id.setText(itemID);
        holder.item_name.setText(itemName);
        holder.item_description.setText(itemDescription);
        holder.item_value.setText(totalPriceRow);
        holder.item_quantity.setText(itemQuantity);
        holder.item_quantity.setVisibility(View.VISIBLE);

        // Handle selection
        holder.itemView.setOnClickListener(v -> toggleSelection(holder));
        if (localSelection.contains(position))
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.Cornsilk));
        else
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.White));
    }

    /**
     * Tag selected item, called from listener. Mark both color and container.
     * @param holder ViewHolder clicked.
     */
    private void toggleSelection(ViewHolder holder) {
        boolean wasSelected = localSelection.contains(holder.getAdapterPosition());
        if (wasSelected) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.White));
            localSelection.remove(holder.getAdapterPosition());
        }
        else {
            localSelection.add(holder.getAdapterPosition());
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.Cornsilk));
        }
    }

    /**
     * Return the stable ID for the item at <code>position</code>.
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        InvoiceRow item = getItem(position);
        return (item != null) ? item.getItemID() : RecyclerView.NO_ID;
    }

    public void clearSelection() {
        for (int index: localSelection) notifyItemChanged(index);
        this.localSelection.clear();
    }

    public @NonNull Set<Integer> getSelectedPositions() {
        return new TreeSet<>(this.localSelection);
    }

    private static final DiffUtil.ItemCallback<InvoiceRow> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<InvoiceRow>() {
        @Override
        public boolean areItemsTheSame(@NonNull InvoiceRow oldItem, @NonNull InvoiceRow newItem) {
            return oldItem.getItemID() == newItem.getItemID();
        }
        @Override
        public boolean areContentsTheSame(@NonNull InvoiceRow oldItem, @NonNull InvoiceRow newItem) {
            return oldItem.equals(newItem);
        }
        @Override
        public Object getChangePayload(@NonNull InvoiceRow oldItem, @NonNull InvoiceRow newItem) {
            return super.getChangePayload(oldItem, newItem);
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_name;
        public final TextView item_id;
        private final TextView item_description;
        private final TextView item_value;
        private final TextView item_quantity;

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
            this.item_quantity = itemView.findViewById(R.id.RV_item_quantity);
        }
    }
}
