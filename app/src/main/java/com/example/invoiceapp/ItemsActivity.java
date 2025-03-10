package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activity for displaying and managing a list of items.
 * This activity allows users to view, add, edit, sort, and remove items.
 * It uses a RecyclerView to display the items stored in an SQLite database.
 */
public class ItemsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ItemsDialogHelper dialogHelper;
    private List<Item> itemsList;
    private RVAdapterItems RVAdapter;
    private TextView TextViewTotalItems;

    /// Icons ID to unable toggle options (Sort by name / value of items)
    int id_icon_sorted_name, id_icon_sorted_name_selected,
            id_icon_sorted_value, id_icon_sorted_value_selected;
    boolean sortedByName = false, sortedByPrice = false;

    /**
     * Called when the activity is first created. Initializes UI components.
     * Notice the upper casting: List type InvoiceItem, getting Item list.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_manager);
        setIconsIDs();
        this.setTitle("");
        this.dialogHelper = new ItemsDialogHelper(this);
        this.databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        this.itemsList = new ArrayList<>(
                databaseHelper.getAllItems(sortedByName, sortedByPrice, false));
        if (itemsList.isEmpty())
            new AlertDialog.Builder(this).setMessage(R.string.no_data_found).show();
        setupRecyclerView();
    }

    /**
     * Inflates the menu options in the toolbar.
     * @param menu The menu to be inflated.
     * @return True if the menu was successfully created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles menu item selection. Menu included:
     * Edit item by selection, adding new item, remove selected items, sorted by price,
     * sorted by name or both, home page (Activity).
     * @param item The selected menu item.
     * @return True if the action was handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.item_manager_home) {
            finish();  // End the activity
            return true;
        }
        if (itemID == R.id.item_manager_edit_item) {
            editItem();
            return true;
        }
        if (itemID == R.id.item_manager_add_item) {
            addItem();
            return true;
        }
        if (itemID == R.id.item_manager_remove_selected) {
            removeSelectedItems();
            return true;
        }
        if (itemID == R.id.item_manager_sorted_name) {
            // Toggle option using boolean
            if (!sortedByName) item.setIcon(this.id_icon_sorted_name_selected);
            else item.setIcon(this.id_icon_sorted_name);
            sortedByName = !sortedByName;
            setResultBy();
            return true;
        }
        if (itemID == R.id.item_manager_sorted_value) {
            // Toggle option using boolean
            if (!sortedByPrice) item.setIcon(this.id_icon_sorted_value_selected);
            else item.setIcon(this.id_icon_sorted_value);
            sortedByPrice = !sortedByPrice;
            setResultBy();
            return true;
        }
        if (itemID == R.id.home_info) {
            displaySnackBar(getString((R.string.info_about_us)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setResultBy() {
        if (this.itemsList.isEmpty()) return;
        if (this.sortedByName && this.sortedByPrice) {
            Collections.sort(this.itemsList, Item::compareToValueName);
            updateItemListRV(false);
        }
        else if (this.sortedByName) {
            Collections.sort(this.itemsList, Item::compareToByName);
            updateItemListRV(false);
        }
        else if (this.sortedByPrice) {
            Collections.sort(this.itemsList, Item::compareToByValue);
            updateItemListRV(false);
        }
        this.RVAdapter.clearSelection();
    }

    /**
     * Updating (Notify) the RV for any changes such as: Editing item, new item, or removing items.
     * @param submitNewList If new item added, or removed items. Else, edited item, will updated.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void updateItemListRV(boolean submitNewList) {
        if (submitNewList) {
            this.RVAdapter.submitList(this.itemsList);
            this.TextViewTotalItems.setText(getString(R.string.item_manager_total_items,
                    itemsList.size()));
        }

        this.RVAdapter.notifyDataSetChanged();
    }

    /** Prompts the user to confirm item removal and removes selected items if confirmed. */
    private void removeSelectedItems() {
        if (RVAdapter.isSelectedEmpty()) {
            displaySnackBar(getString(R.string.selection_no_input_selected));
            return;
        }
        List<Item> selectedItems = RVAdapter.getSelectedItems();
        this.dialogHelper.ConfirmationRemoveDialog(selectedItems, new DialogCallback<>() {
            @Override
            public void onSuccess(Item item) {
                // NOTE: the return item is null
                if (databaseHelper.removeItems(RVAdapter.getSelectedIDs()).isEmpty())
                    displaySnackBar(getString(R.string.error_action_data_base));
                else {
                    updateItemListRemoved(selectedItems);
                    displaySnackBar(getString((R.string.item_manager_selected_removed)));
                }
            }
            @Override
            public void onFailure(String failMSG) { displaySnackBar(failMSG); }
        });
    }

    /** Opens a dialog for adding a new item, and updating the RV */
    private void addItem() {
        this.dialogHelper.NewItemDialog(new DialogCallback<>() {
            @Override
            public void onSuccess(Item item) {
                int newItemID = databaseHelper.addNewItem(item); // We need the new ID
                if (newItemID == -2) {
                    displaySnackBar(getString(R.string.item_manager_exists_item));
                 }
                else if (newItemID > 0) {
                    displaySnackBar(getString((R.string.item_manager_item_added)));
                    Item newItem = new Item(
                            newItemID, item.getName(), item.getDescription(), item.getValue());
                    itemsList.add(newItem);
                    RVAdapter.notifyItemInserted(0);
                    setResultBy();
                }
                else displaySnackBar(getString(R.string.error_action_data_base));
            }
            @Override
            public void onFailure(String failMSG) {
                displaySnackBar(failMSG);
            }
        });
    }

    /** Opens a dialog for editing a selected item. Will use 'updateItem' for updating the RV. */
    private void editItem() {
        Item tempTest = this.RVAdapter.getOneSelectedItem();
        if (tempTest == null) {
            displaySnackBar(getString((R.string.selection_no_input_selected)));
            return;
        }

        // Get the selected item from RV, pop dialog with this argument,
        // and when DialogCallBack onSuccess is called, catch the updated item with the same ID,
        // and call the database helper for edit.

        this.dialogHelper.EditItemDialog(tempTest, new DialogCallback<>() {
            @Override
            public void onSuccess(Item item) {
                if (databaseHelper.editItem(item)) {
                    updateItem(item);
                    displaySnackBar(getString((R.string.dialog_edited)));
                    updateItemListRV(true);
                }
                else displaySnackBar(getString((R.string.action_failed)));
                RVAdapter.clearSelection();
            }
            @Override
            public void onFailure(String failMSG) {
                displaySnackBar(failMSG);
            }
        });
    }

    /**
     * Will update the activity item list, not the data base.
     * @param updatedItem Item to update on this list.
     */
    private void updateItem(Item updatedItem) {
        if (updatedItem == null) return;
        for (int i = 0; i < this.itemsList.size(); i++) {
            if (this.itemsList.get(i).getID() == updatedItem.getID()) {
                this.itemsList.set(i, updatedItem);
                updateItemListRV(true);
                break;
            }
        }
    }

    /**
     * Will update the activity item list, not the data base.
     * Will clear the removed items from the RV, and reset the order of the displayed list on RV.
     * @param selectedItemRemoved Items on this list for remove.
     */
    private void updateItemListRemoved(@NonNull List<Item> selectedItemRemoved) {
        if (selectedItemRemoved.isEmpty()) return;
        for (Item item: selectedItemRemoved) this.itemsList.remove(item);
        updateItemListRV(true);
        this.RVAdapter.clearSelection();
    }

    /** Display msg as SnackBar */
    private void displaySnackBar(@NonNull String msg) {
        /// GUI Elements
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /** Initializes icon IDs. */
    private void setIconsIDs() {
        this.id_icon_sorted_name = R.drawable.items_sorted_by_name;
        this.id_icon_sorted_name_selected = R.drawable.items_sorted_by_name_selected;
        this.id_icon_sorted_value = R.drawable.items_sorted_by_price;
        this.id_icon_sorted_value_selected = R.drawable.items_sorted_by_price_selected;
    }

    /** Sets up the RecyclerView with its adapter and grid (2 in row) layout manager. */
    private void setupRecyclerView() {
        this.RVAdapter = new RVAdapterItems(this);
        RecyclerView itemsRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager RVLayout = new GridLayoutManager
                (this, 2, RecyclerView.VERTICAL, false);
        itemsRecyclerView.setLayoutManager(RVLayout);
        itemsRecyclerView.setAdapter(this.RVAdapter);
        itemsRecyclerView.setClipToPadding(true);
        this.RVAdapter.submitList(this.itemsList);
        this.TextViewTotalItems = findViewById(R.id.items_activity_info);
        this.TextViewTotalItems.setText(getString(R.string.item_manager_total_items, itemsList.size()));
    }

}