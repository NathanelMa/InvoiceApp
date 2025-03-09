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
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

/**
 * Dialog offer 2 methods: Updating item or adding a new item.
 * Both responsible for layout and interaction including msg display for each action.
 * NOTE: No operation on database.
 */
public class ItemsDialogHelper {

    private final Context context;

    /**
     * @param context Activity context.
     */
    public ItemsDialogHelper(Context context) {
        this.context = context;
    }

    /**
     * Displays a dialog for adding new item. If the operation is
     * successful, the callback's onSuccess is called with the new item.
     * Otherwise, onFailure is called.
     * @param callback Callback to notify the calling activity of success or failure.
     */
    public void NewItemDialog(@NonNull DialogCallback<Item> callback) {
        View dialogView = LayoutInflater.from(this.context).
                inflate(R.layout.activity_items_manager_dialog, null);
        EditText editItemName = dialogView.findViewById(R.id.items_dialog_item_name);
        EditText editDescription = dialogView.findViewById(R.id.items_dialog_item_description);
        EditText editPrice = dialogView.findViewById(R.id.items_dialog_item_price);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.dialog_add_button), null)
                .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                .create();
        dialog.setCancelable(false);
        dialog.show();

        /// Positive button logic
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String itemName = editItemName.getText().toString().trim();
            String itemDescription = editDescription.getText().toString().trim();
            String itemPrice = editPrice.getText().toString().trim();
            if (!itemName.isEmpty() && !itemDescription.isEmpty() && !itemPrice.isEmpty()) {
                Object test = Item.checkValidData(itemName, itemDescription, itemPrice);
                if (test instanceof Item) {
                    Item tempItem = (Item) test;
                    callback.onSuccess(tempItem);
                    dialog.dismiss();
                }
                else {
                    editDescription.setText("");
                    editItemName.setText("");
                    editPrice.setText("");
                    Snackbar snackbar = Snackbar.make(dialogView, (String) test, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
            else {
                Snackbar snackbar = Snackbar.make(dialogView,
                        context.getString(R.string.action_needed), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });

        /// Negative button logic
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            callback.onFailure(context.getString(R.string.dialog_canceled_by_user));
            dialog.dismiss();
        });
    }

    /**
     * Displays a dialog for editing selected item.
     * @param callback Callback to notify the calling activity of success or failure.
     */
    public void EditItemDialog(@NonNull Item prevItem, @NonNull DialogCallback<Item> callback) {
        View dialogView = LayoutInflater.from(this.context).
                inflate(R.layout.activity_items_manager_dialog, null);
        TextView title = dialogView.findViewById(R.id.items_dialog_title);
        title.setText(context.getString(R.string.dialog_title_edit_selected));
        EditText editItemName = dialogView.findViewById(R.id.items_dialog_item_name);
        EditText editDescription = dialogView.findViewById(R.id.items_dialog_item_description);
        EditText editPrice = dialogView.findViewById(R.id.items_dialog_item_price);
        editItemName.setText(prevItem.getName());
        editDescription.setText(prevItem.getDescription());
        editPrice.setText(String.valueOf(prevItem.getValue()));
        AlertDialog dialog = new AlertDialog.Builder(this.context)
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.dialog_save), null)
                .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                .create();
        dialog.setCancelable(false);
        dialog.show();

        // Positive button logic
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String itemName = editItemName.getText().toString().trim();
            String itemDescription = editDescription.getText().toString().trim();
            String itemPrice = editPrice.getText().toString().trim();

            if (!itemName.isEmpty() && !itemDescription.isEmpty() && !itemPrice.isEmpty()) {
                Object test = Item.checkValidData(itemName, itemDescription, itemPrice, prevItem.getID());
                if (test instanceof Item) {
                    Item tempItem = (Item) test;
                    callback.onSuccess(tempItem);
                    dialog.dismiss();
                }
                else {
                    editItemName.setText(prevItem.getName());
                    editDescription.setText(prevItem.getDescription());
                    editPrice.setText(String.valueOf(prevItem.getValue()));
                    Snackbar snackbar = Snackbar.make(dialogView,
                            (String) test, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
            else {
                Snackbar snackbar = Snackbar.make(dialogView,
                       context.getString(R.string.action_needed), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
        // Negative button logic
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            callback.onFailure(context.getString(R.string.dialog_canceled_by_user));
            dialog.dismiss();
        });
    }

    /**
     * Display dialog with the names of selected items for cancel or confirm removing.
     * OnSuccess, callback will contain null item.
     * @param selectedItems List of the selected items.
     * @param callback  Callback to notify the calling activity of success or failure.
     */
    public void ConfirmationRemoveDialog(@NonNull List<Item> selectedItems,
                                         @NonNull DialogCallback<Item> callback) {
        // Check if the selected contain at least one item
        if (selectedItems.isEmpty())
            callback.onFailure(context.getString(R.string.selection_no_input_selected));

        // Else, wait to confirmation from user for removing, and do if so.
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < selectedItems.size(); i++) {
                stringBuilder.append(selectedItems.get(i).getName());
                if (i + 1 != selectedItems.size()) stringBuilder.append(", ");
            }
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(stringBuilder.toString())
                    .setTitle(context.getString(R.string.dialog_title_removing_selected))
                    .setPositiveButton(context.getString(R.string.dialog_remove), null)
                    .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                    .create();
            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    callback.onSuccess(null);
                    dialog.dismiss();
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                    callback.onFailure(context.getString(R.string.dialog_canceled_by_user));
                    dialog.dismiss();
                });
            });
            dialog.setIcon(R.drawable.warning_ic);
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}
