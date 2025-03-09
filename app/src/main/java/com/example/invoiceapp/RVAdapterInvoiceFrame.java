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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** RecyclerView Adapter for composing invoice */
public class RVAdapterInvoiceFrame
        extends ListAdapter<InvoiceFrame, RVAdapterInvoiceFrame.ViewHolder> {

    private final Context context;
    public final Set<Integer> localSelection; // Buffer selection by user. Contain positions
    private OnItemLongClickListener<InvoiceFrame> onItemLongClickListener = null;

    public RVAdapterInvoiceFrame(@NonNull Context context) {
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
        InvoiceFrame invoiceFrame = getItem(position);
        if (invoiceFrame == null) return;

        // Get data from invoice row (Strings)
        String invoiceID = FormatUtils.formatSerialNumber(invoiceFrame.getID());
        String invoiceDate = "";
        try { invoiceDate = FormatUtils.changeFullDateFormat(invoiceFrame.getDate()); }
        catch (Exception e) { Log.i("Invoice Manager", "Error patching invoice date"); }
        String invoiceTotalPrice = FormatUtils.formatCurrency(invoiceFrame.getTotalPrice());

        // Set the text data on holder
        holder.item_id.setText(invoiceID);
        holder.item_name.setText(invoiceDate);
        holder.item_value.setText(invoiceTotalPrice);

        // Handle selection
        holder.itemView.setOnClickListener(v -> toggleSelection(holder));

        // Set long press
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                InvoiceFrame selectedItem = null;
                long rowID = getItemId(holder.getAdapterPosition());
                for (InvoiceFrame f: getCurrentList()) if (f.getID() == rowID) {
                        selectedItem = f;
                        break;
                    }
                if (selectedItem != null) {
                    onItemLongClickListener.onItemLongClick(selectedItem);
                    return true;
                }
            }
            return false;
        });

        if (localSelection.contains(position))
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.Cornsilk));
        else
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.White));
    }

    /**
     * Return the stable ID for the item at <code>position</code>.
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    @Override
    public long getItemId(int position) {
        InvoiceFrame item = getItem(position);
        return (item != null) ? item.getID() : RecyclerView.NO_ID;
    }

    /**
     * Selecting one frame from the list. Will update the localSelection (Set) list.
     * @param holder ViewHolder to be selected.
     */
    private void toggleSelection(ViewHolder holder) {
        boolean wasSelected = localSelection.contains(holder.getAdapterPosition());
        if (wasSelected) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.White));
            localSelection.remove(holder.getAdapterPosition());
        }
        else {
            localSelection.add(holder.getAdapterPosition());
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.BlanchedAlmond));
        }
    }

    /**
     * Will clear the selection list.
     * */
    public void clearSelection() {
        for (int index: localSelection) notifyItemChanged(index);
        this.localSelection.clear();
    }

    /**
     * Return if the one or more selected frames.
     * @return True if empty.
     */
    public boolean isSelectedEmpty() {
        return this.getSelectedPositions().isEmpty();
    }

    /**
     * Return the selected positions as in this adapter list.
     * @return Set of all indexes of those been selected
     */
    public @NonNull Set<Integer> getSelectedPositions() {
        return new TreeSet<>(localSelection);
    }

    /**
     * Return the selected frames.
     * @return List of selected frames.
     */
    public @NonNull List<InvoiceFrame> getSelectedFrames() {
        List<InvoiceFrame> frameList = new ArrayList<>();
        for (Integer pos: this.localSelection) frameList.add(getItem(pos));
        return frameList;
    }

    private static final DiffUtil.ItemCallback<InvoiceFrame> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull InvoiceFrame oldItem, @NonNull InvoiceFrame newItem) {
            return oldItem.getID() == newItem.getID();
        }
        @Override
        public boolean areContentsTheSame(@NonNull InvoiceFrame oldItem, @NonNull InvoiceFrame newItem) {
            return oldItem.equals(newItem);
        }
        @Override
        public Object getChangePayload(@NonNull InvoiceFrame oldItem, @NonNull InvoiceFrame newItem) {
            return super.getChangePayload(oldItem, newItem);
        }
    };

    /**
     * Set a listener for item long click events.
     * @param listener The listener to handle item long clicks.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener<InvoiceFrame> listener) {
        this.onItemLongClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_name;
        private final TextView item_id;
        private final TextView item_value;

        /**
         * Constructor that initializes the UI components for each Item.
         * @param itemView The root view of the Item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ImageView icon = itemView.findViewById(R.id.with_icon);
            icon.setImageResource(R.drawable.invoice_frame);
            this.item_name = itemView.findViewById(R.id.RV_item_name);
            this.item_id = itemView.findViewById(R.id.RV_item_ID);
            this.item_value = itemView.findViewById(R.id.RV_item_value);
            itemView.findViewById(R.id.RV_item_description).setVisibility(View.GONE);
            this.item_name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            this.item_id.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }
}
