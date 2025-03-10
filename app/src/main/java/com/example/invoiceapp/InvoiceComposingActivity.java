package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This activity allow the user composing a new/edit invoice using selection from current items.
 * At the end, if the user choose to save the invoice, it will be save into database.
 * User can remove selected rows, reduces selected rows, or add automatic to excited item.
 */
public class InvoiceComposingActivity extends AppCompatActivity {

    /*
        If init for edit:
            It will draw all data from database, based on the invoice ID.
            Safe Operation in Database only:
                after composing/edit rows, when choose to save the current edited invoice,
                it will remove all related data from database, and place with the same ID.
                Also, removed for the invoice frame itself, and added it with the same ID.
            Safe operation in this activity:
                if user cancel, nothing will be change on database.
     */

    private DatabaseHelper databaseHelper;
    private ShoppingCard shoppingCard;
    private List<Item> itemsList;

    /// Temp selected values (Item, Amount) are update live according the user
    private Item selectedItem;
    private int selectedQuantity;
    private boolean isActivityForEdit;

    /// GUI elements
    private RecyclerView RV;
    private RVAdapterInvoiceRows RVAdapter;
    private Spinner spinnerAddItem;
    private EditText editQuantity;
    private TextView totalPriceItemTempTV, totalNumberItemsTV;
    private TextView totalComposedInvoicePriceTV;

    /**
     * Called when the activity is first created. Initializes UI components.
     * @param savedInstanceState Prev instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_composing);
        this.setTitle("");
        this.databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        int invoiceID = (int) databaseHelper.getNextInvoiceID();
        this.itemsList = this.databaseHelper.getAllItems(true, true, false);

        if (this.itemsList.isEmpty())
            displaySnackBar(getString(R.string.invoice_compose_no_items_found));

        // Check if asked if edit invoice: Init activity for edit an invoice frame
        Intent intent = getIntent();
        long requestedInvoiceID = intent.getLongExtra("InvoiceFrameID", -1);
        if (requestedInvoiceID != -1) {
            this.setTitle(getString(R.string.invoice_compose_edit_mode_title));
            this.isActivityForEdit = true;
            List<InvoiceRow> rows = this.databaseHelper.getAllRowsByID(requestedInvoiceID, true);
            this.shoppingCard = new ShoppingCard(rows, requestedInvoiceID);
        }
        // If not for edit, init normal composing invoice from scratch
        else {
            this.shoppingCard = new ShoppingCard(null, invoiceID);
        }
        setupRecyclerView();
        setupCardGUI();
    }

    /**
     * Each time submit a new list, ListAdapter runs DiffUtil on a background thread to
     * compute the difference between the new list and the old list: Any change.
     */
    @SuppressLint({"NotifyDataSetChanged"})
    private void updateCardGUI() {
        this.totalNumberItemsTV.setText(getString(R.string.item_manager_total_items,
                this.shoppingCard.getTotalItems()));
        this.totalComposedInvoicePriceTV.setText(FormatUtils.formatCurrency(shoppingCard.getTotalPrice()));
        this.RVAdapter.submitList(this.shoppingCard.getComposedList());
        this.RVAdapter.notifyDataSetChanged();
    }

    private void addRow() {
        if (itemsList.isEmpty()) return;
        InvoiceRow newInvoiceRow
                = new InvoiceRow(shoppingCard.getInvoiceID(), this.selectedQuantity, this.selectedItem);
        this.shoppingCard.addRow(newInvoiceRow);
        updateCardGUI();
        this.RVAdapter.clearSelection();
    }

    /**
     * Remove selected items, can be done with multiple rows.
     * If no selected items, notify the user.
     */
    private void removeSelectedItems() {
        this.shoppingCard.removeSelectedItems(new HashSet<>(this.RVAdapter.getSelectedPositions()));
        updateCardGUI();
        this.RVAdapter.clearSelection();
        displaySnackBar(getString(R.string.invoice_compose_selected_items_removed));
    }

    /**
     * Before using make sure updating the 'selectedItem' and the 'selectedQuantity'.
     * Updating the row of total price for selected item and the selected amount of it.
     * Note: This is not the added row price.
     */
    private void updateItemTempPrice() {
        double totalPriceItemTemp = this.selectedItem.getValue() * this.selectedQuantity;
        this.totalPriceItemTempTV.setText(FormatUtils.formatCurrency(totalPriceItemTemp));
    }

    /**
     * Reduce the quantity of selected rows. If the quantity of one of them is zero,
     * remove it and notify the recyclerViewAdapterBasicItems. If no selected items, notify the user.
     * It can be done with multiple rows.
     */
    private void reduceSelectedItemQuantity() {
        List<Integer> targetSelection = new ArrayList<>(this.RVAdapter.getSelectedPositions());
        if (targetSelection.isEmpty()) {
            displaySnackBar(getString(R.string.selection_no_input_selected));
            return;
        }
        else {
            if (this.shoppingCard.reduceSelectedItemQuantity(targetSelection))
                this.RVAdapter.clearSelection();
        }
        updateCardGUI();
    }

    private void addSelectedItemQuantity() {
        List<Integer> targetSelection = new ArrayList<>(this.RVAdapter.getSelectedPositions());
        if (targetSelection.isEmpty()) {
            displaySnackBar(getString(R.string.selection_no_input_selected));
        }
        else {
            this.shoppingCard.addSelectedItemsQuantity(targetSelection);
            updateCardGUI();
        }
    }

    private void setupCardGUI() {
        TextView invoiceID_TextView = findViewById(R.id.invoice_ID);
        Button addItemsButton = findViewById(R.id.adding_button);
        FloatingActionButton createInvoiceButton = findViewById(R.id.composing_invoice_create_button);
        FloatingActionButton cancelInvoiceButton
                = findViewById(R.id.composing_invoice_cancel_button);

        invoiceID_TextView.setText(FormatUtils.formatSerialNumber(this.shoppingCard.getInvoiceID()));
        this.totalPriceItemTempTV = findViewById(R.id.textview_total_price_item_added);
        this.totalComposedInvoicePriceTV = findViewById(R.id.textview_total_price_invoice);
        this.totalNumberItemsTV = findViewById(R.id.textview_total_items);
        this.spinnerAddItem = findViewById(R.id.spinner_items);
        this.editQuantity = findViewById(R.id.edit_quantity);
        this.selectedQuantity = 0;
        this.totalNumberItemsTV.setText(getString(R.string.invoice_compose_total_items, this.itemsList.size()));
        this.totalComposedInvoicePriceTV.setText(FormatUtils.formatCurrency(this.shoppingCard.getTotalPrice()));
        /// Setup quantity listener
        editQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    selectedQuantity = Integer.parseInt(s.toString());
                    if (selectedQuantity <= 0) throw new Exception();
                    selectedItem = (Item) spinnerAddItem.getSelectedItem();
                    updateItemTempPrice();
                }
                catch (Exception e) {
                    // Display hint error for non-integer
                    editQuantity.setError(getString(R.string.error_input));
                }
            }
        });

        /// Setup spinner for items from database
        ArrayAdapter<Item> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, this.itemsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinnerAddItem.setAdapter(adapter);

        /// Setup spinner listener
        spinnerAddItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                // Getting at position return Object type. Safe casting
                selectedItem = (Item) parentView.getItemAtPosition(position);
                updateItemTempPrice();
                ((TextView) parentView.getChildAt(0)).setTextColor(getResources().getColor(R.color.Navy));
                ((TextView) parentView.getChildAt(0)).setTextSize(26);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                ((TextView) parentView.getChildAt(0)).setTextSize(26);
            }
        });

        /// Setup add row button listener
        addItemsButton.setOnClickListener(view -> {
            if (this.selectedQuantity <= 0) return;
            addRow();
        });

        /// Setup cancel and create listener
        cancelInvoiceButton.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.invoice_compose_conf_cancel))
                    .setTitle("")
                    .setPositiveButton(getString(R.string.dialog_confirm), null)
                    .setNegativeButton(getString(R.string.dialog_cancel), null)
                    .create();
            dialog.setOnShowListener(d -> {
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            finishActivityAndCallManager();
                            dialog.dismiss();
                        });
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                        .setOnClickListener(v -> {
                            /// Continue editing invoice
                            dialog.dismiss();
                        }); });
            dialog.show();
        });

        createInvoiceButton.setOnClickListener(view -> composeInvoice());
    }

    /** Sets up the RecyclerView with its adapter and layout manager. */
    private void setupRecyclerView() {
        this.RV = findViewById(R.id.recycler_view);
        this.RVAdapter = new RVAdapterInvoiceRows(this);
        LinearLayoutManager layoutManager;

        if (isActivityForEdit)
            layoutManager = new GridLayoutManager
                    (this, 2,RecyclerView.VERTICAL, false);
        else
            layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        RV.setLayoutManager(layoutManager);
        RV.setAdapter(this.RVAdapter);
        this.RVAdapter.submitList(this.shoppingCard.getComposedList());
    }

    private void changeLayoutRV() {
        if (RV.getLayoutManager() instanceof GridLayoutManager) {
            RV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
        else {
            RV.setLayoutManager(new GridLayoutManager
                    (this, 2,RecyclerView.VERTICAL, false));
        }
    }

    /**
     * Create the invoice frame from the composedInvoiceRows composed by user.
     * Finish the activity at the end. Both for edit mode and compose new invoice.
     */
    private void composeInvoice() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.invoice_compose_finish_comp))
                .setTitle("")
                .setPositiveButton(getString(R.string.dialog_confirm), null)
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        if (this.isActivityForEdit) finishEdit();
                        else finishCompose();
                        dialog.dismiss();
                    });
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener(v -> {
                        /// Continue editing invoice
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private void finishCompose() {
        if (databaseHelper.addComposedInvoice(shoppingCard.getComposedList()))
            displaySnackBar(getString(R.string.invoice_compose_saved));
        finishActivityAndCallManager();
    }

    private void finishEdit() {
        if (databaseHelper.editInvoice((int) this.shoppingCard.getInvoiceID(), this.shoppingCard.getComposedList()))
            displaySnackBar(getString(R.string.invoice_compose_edit_saved));
        finishActivityAndCallManager();
    }

    /** Suppress BackPressed from user, in order to prevent data lost on composed invoice. */
    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        // Suppress back call from user
    }

    /**
     * Inflates the menu options in the toolbar.
     * @param menu The menu to be inflated.
     * @return True if the menu was successfully created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_invoice_composing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles menu invoice composing.
     * @param m The selected menu item.
     * @return True if the action was handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem m) {
        if (m.getItemId() == R.id.invoice_composing_reduce_amount) {
            reduceSelectedItemQuantity();
            return true;
        }
        if (m.getItemId() == R.id.invoice_composing_remove_row) {
            removeSelectedItems();
            return true;
        }
        if (m.getItemId() == R.id.add_quantity) {
            addSelectedItemQuantity();
            return true;
        }
        if (m.getItemId() == R.id.change_layout) {
            this.changeLayoutRV();
            return true;
        }
        if (m.getItemId() == R.id.invoice_composing_clear_selection) {
            this.RVAdapter.clearSelection();
            return true;
        }
        if (m.getItemId() == R.id.home_info) {
            Toast.makeText(this, R.string.info_about_us, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(m);
    }

    private void finishActivityAndCallManager() {
        Intent intent = new Intent(this, InvoiceManagerActivity.class);
        startActivity(intent);
        finish();
    }

    /** Display msg as SnackBar */
    private void displaySnackBar(@NonNull String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_LONG);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);
        snackbar.show();
    }
}
