package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * InvoiceRow manager for adding and display invoice from database.
 * This activity allow user the following operation with invoices:
 * 1. Look for invoice by number (invoiceID).
 * 2. Show all invoice by given date.
 * 3. Composing a new invoice - Starting a new activity.
 * 4. Print/Sharing selected (full) invoice frame.
 * NOTE: First invoice frame list display sorted be recent
 */
public class InvoiceManagerActivity extends AppCompatActivity {

    // Data from database
    private DatabaseHelper database;
    private List<InvoiceFrame> allInvoiceFrames;

    // GUI Elements
    private TextView editDate;
    private String selectedDate;
    private SearchView searchViewID;
    private LinearLayoutManager gridLayoutManagerRV;
    private ImageButton sortByRecentButton, sortByValueButton;
    private Button dateSearchButton;
    private RVAdapterInvoiceFrame RVAdapter; // Adapter for the GUI
    private InvoiceManagerDialog invoiceManagerDialog; // Dialog helper for different operation

    /*
        Sorting methods on the frames list. 3 Different modes on the list.
        Icon colors: Yellow for sort by, green for reverse sort, white for non sort.
        All modes, are valid on the current list, and depend on the searchByMode.
        If the searchByMode is True: All sorting will effect the result.
        Cancel this mode via press the same button (Search by date).
     */
    private static final int
            NON_SORT = 0,
            SORT = 1,
            SORT_REVERSE = 2;

    private int sortColor, nonSortColor, sortReverseColor;
    private int sortRecentMode = NON_SORT, sortValueMode = NON_SORT;
    private boolean searchByMode = false;

    /**
     * Patch all invoice frames and put into the RV, order by date.
     * Will set all GUI elements as well. If no data at the data base, will notify the user.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.invoice_manager_activity_title));
        setContentView(R.layout.activity_invoice_manager);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        this.selectedDate = "";
        this.database = DatabaseHelper.getInstance(getApplicationContext());
        this.invoiceManagerDialog = new InvoiceManagerDialog(
                database.getAllItems(false, false, true), this);
        this.allInvoiceFrames = new ArrayList<>(database.getByRecentInvoices());
        setupRecyclerView();
        setupDatePicker();
        setupSearchByID();
        setupSortingOptions();
        setupFloatingButtonComposeInvoice();
        setUpRemove();
        setupClearSelection();
        setupEditInvoice();
        String msg = getString(R.string.invoice_manager_activity_no_data);

        // Init sorting by recent
        if (!this.allInvoiceFrames.isEmpty()) {
            toggleSortedByRecent();
            SortBy();
        }
        else displaySnackBar(msg);
    }

    /**
     * Removed selected invoices (IDs) frames from current list:
     * First removing from the this list, after, removing from database.
     * If removing from database failing, will reload the prev list.
     * Else, will update the current list, and notify the RV for removed items.
     * @SuppressLint NotifyDataSetChanged for the RV (must).
     */
    @SuppressLint("NotifyDataSetChanged")
    private void removeSelectedInvoices() {
        Set<Integer> IDs = new TreeSet<>();
        List<InvoiceFrame> frameList = RVAdapter.getSelectedFrames();

        for (InvoiceFrame rmv_frame: frameList) {
            this.allInvoiceFrames.remove(rmv_frame);
            IDs.add((int) rmv_frame.getID()); // Casting from long
        }
        if (this.database.RemoveInvoiceFrames(IDs)) {
            // If successes, submit the update list (All frames removed) via the setResultBy.
            SortBy();
            displaySnackBar(getString(R.string.invoice_manager_activity_total_removed, IDs.size()));
        }
        else {
            displaySnackBar(getString(R.string.error_action_data_base));
            this.allInvoiceFrames = this.database.getByRecentInvoices(); // Reload undo list
            SortBy(); // Resort
        }
        this.RVAdapter.clearSelection();
        this.RVAdapter.notifyDataSetChanged(); // Either way there is a new list
    }

    /** Toggle button for recent sort option - Setup color and boolean variable only */
    private void toggleSortedByRecent() {
        sortRecentMode = (sortRecentMode + 1) % 3;
        if (sortRecentMode == NON_SORT) {
            sortByRecentButton.setColorFilter(nonSortColor, PorterDuff.Mode.SRC_IN);
        }
        else if (sortRecentMode == SORT) {
            sortByRecentButton.setColorFilter(sortColor, PorterDuff.Mode.SRC_IN);
        }
        else {
            sortByRecentButton.setColorFilter(sortReverseColor, PorterDuff.Mode.SRC_IN);
        }
    }

    /** Toggle button for values sort option - Setup color and boolean variable */
    private void toggleSortedByValue() {
        sortValueMode = (sortValueMode + 1) % 3;
        if (sortValueMode == NON_SORT) {
            sortByValueButton.setColorFilter(nonSortColor, PorterDuff.Mode.SRC_IN);
        }
        else if (sortValueMode == SORT) {
            sortByValueButton.setColorFilter(sortColor, PorterDuff.Mode.SRC_IN);
        }
        else {
            sortByValueButton.setColorFilter(sortReverseColor, PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * Reset sorting modes: Both by date and value.
     * Patch the updated frames list from database, and notify the RV.
     * Will clear the current selection as well.
     */
    private void resetToggleOption() {
        sortRecentMode = NON_SORT;
        sortValueMode = NON_SORT;
        sortByValueButton.setColorFilter(nonSortColor, PorterDuff.Mode.SRC_IN);
        sortByRecentButton.setColorFilter(nonSortColor, PorterDuff.Mode.SRC_IN);
        this.allInvoiceFrames = new ArrayList<>(this.database.getByRecentInvoices());
        int currentPosition = gridLayoutManagerRV.findFirstVisibleItemPosition();
        this.RVAdapter.submitList(allInvoiceFrames,() -> gridLayoutManagerRV.scrollToPosition(currentPosition));
        this.RVAdapter.clearSelection();
    }

    /**
     * Set the sorting mode as toggled by user.
     * Will notify the user for current sort mode by display msg (SnakeBar).
     * */
    private void SortBy() {
        if (this.allInvoiceFrames.isEmpty()) return;
        this.RVAdapter.clearSelection();
        int numFrames = this.allInvoiceFrames.size();

        // Both filter (reversed or normal)
        if (sortRecentMode == SORT_REVERSE && sortValueMode == SORT_REVERSE) {
            displaySnackBar(getString(R.string.invoice_manager_sort_both_rev, numFrames));
            Collections.sort(this.allInvoiceFrames,
                    Collections.reverseOrder(InvoiceFrame::compareToValueDate));
        }
        else if ((sortRecentMode == SORT  || sortRecentMode == SORT_REVERSE) &&
                (sortValueMode == SORT  || sortValueMode == SORT_REVERSE)) {
            displaySnackBar(getString(R.string.invoice_manager_sort_both, numFrames));
            Collections.sort(this.allInvoiceFrames, InvoiceFrame::compareToDateValue);
        }
        // Single filter
        else if (sortRecentMode == SORT) {
            displaySnackBar(getString(R.string.invoice_manager_sort_date, numFrames));
            Collections.sort(this.allInvoiceFrames, InvoiceFrame::compareToByDate);
        }
        else if (sortValueMode == SORT) {
            displaySnackBar(getString(R.string.invoice_manager_sort_value, numFrames));
            Collections.sort(this.allInvoiceFrames, InvoiceFrame::compareToByValue);

        }
        else if (sortRecentMode == SORT_REVERSE) {
            displaySnackBar(getString(R.string.invoice_manager_sort_date_rev, numFrames));
            Collections.sort(this.allInvoiceFrames,
                    Collections.reverseOrder(InvoiceFrame::compareToByDate));
        }
        else if (sortValueMode == SORT_REVERSE) {
            displaySnackBar(getString(R.string.invoice_manager_sort_value_rev, numFrames));
            Collections.sort(this.allInvoiceFrames,
                    Collections.reverseOrder(InvoiceFrame::compareToByValue));
        }
        // Non filter, random order
        else {
            displaySnackBar(getString(R.string.invoice_manager_sort_random, numFrames));
            Collections.shuffle(this.allInvoiceFrames);
        }
        // Save the last point display, and submit the new list
        int currentPosition = gridLayoutManagerRV.findFirstVisibleItemPosition();
        this.RVAdapter.submitList(allInvoiceFrames,() ->
                gridLayoutManagerRV.scrollToPosition(currentPosition));
    }

    /**
     * Show full invoice, including the rows and the headers for the selected invoice.
     * @param frame Selected InvoiceFrame
     */
    public void showFullInvoice(InvoiceFrame frame) {
        if (frame == null) return;
        if (frame.isEmpty()) return;
        List<InvoiceRow> invoiceRows = this.database.getAllRowsByID(frame.getID(), true);
        invoiceManagerDialog.displayInvoice(frame, invoiceRows);
    }

    /** Search by date all invoices frame. Updating 'resultByDateList'. */
    private void searchByDate() {
        searchViewID.setQuery("", false);
        searchViewID.clearFocus();
        String msgRes = getString(R.string.invoices_manager_search_date_no_output, selectedDate);
        if (selectedDate != null && !selectedDate.isEmpty()) {
            List<InvoiceFrame> filteredInvoices = database.getInvoicesByDate(selectedDate);
            if (!filteredInvoices.isEmpty()) {
                msgRes = getString(R.string.invoice_manager_activity_found_invoices,
                        filteredInvoices.size());
                this.allInvoiceFrames = new ArrayList<>(filteredInvoices);
                this.RVAdapter.submitList(this.allInvoiceFrames);
                this.searchByMode = true;
                this.dateSearchButton.setText(getString(R.string.invoice_manager_button_undo_search));
                SortBy();
            }
            else {
                this.searchByMode = false;
                // No changes on the current list.
            }
            displaySnackBar(msgRes);
        }
    }

    /** Search the current search ID and update the 'searchResultInvoices' list */
    private void searchByID() {
        String query = searchViewID.getQuery().toString();
        if (!query.isEmpty()) {
            try {
                int invoiceID = Integer.parseInt(query);
                String msgRes = getString(R.string.invoice_manager_search_by_id_no_found, invoiceID);
                // Frame searched by user
                InvoiceFrame invoiceFrameResID;
                invoiceFrameResID = database.getInvoiceById(invoiceID);
                if (invoiceFrameResID != null) {
                    this.searchByMode = true;
                    this.dateSearchButton.setText(R.string.invoice_manager_button_undo_search);
                    RVAdapter.submitList(Collections.singletonList(invoiceFrameResID));
                }
                else
                    displaySnackBar(msgRes);
            }
            catch (NumberFormatException e) {
                displaySnackBar(getString(R.string.invoice_manager_activity_invalid_invoice_id));
            }
        }
    }

    /** Setup search option */
    private void setupSearchByID() {
        this.searchViewID = findViewById(R.id.invoice_manager_search_bar);
        Button searchByIDButton = findViewById(R.id.invoice_manager_search_id_button);
        searchByIDButton.setOnClickListener(v -> searchByID());
        EditText searchEditText = searchViewID.findViewById(androidx.appcompat.R.id.search_src_text);
        // Set GUI look
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.WHITE);
            searchEditText.setHintTextColor(Color.GRAY);
            searchEditText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            searchEditText.setTextSize(22);
        }
        // Set listener
        searchViewID.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return true; }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) { SortBy(); }
                return false;
            }
        });
    }

    /** Set recycler view with the long click listener calling showDialogFullInvoice */
    private void setupRecyclerView() {
        this.RVAdapter = new RVAdapterInvoiceFrame(this);
        // GUI element for display all invoice frames
        RecyclerView itemsRecyclerView = findViewById(R.id.recycler_view);
        this.gridLayoutManagerRV = new GridLayoutManager
                (this, 2,RecyclerView.VERTICAL, false);
        itemsRecyclerView.setLayoutManager(gridLayoutManagerRV);
        itemsRecyclerView.setAdapter(this.RVAdapter);
        this.RVAdapter.submitList(this.allInvoiceFrames);
        this.gridLayoutManagerRV.setOrientation(RecyclerView.VERTICAL);
        this.gridLayoutManagerRV.setSmoothScrollbarEnabled(true);
        this.gridLayoutManagerRV.scrollToPosition(0);
        // Set show full invoice, using long click listener from the RV
        this.RVAdapter.setOnItemLongClickListener(this::showFullInvoice);
    }

    /**
     * Set up sorting options: Sort by recent/value.
     * Each click, will update the toggle option (mode from 0 to 2 included).
     * Updating RVAdapter list as well.
     */
    private void setupSortingOptions() {
        this.sortByRecentButton = findViewById(R.id.button_sort_recent);
        this.sortByValueButton = findViewById(R.id.button_sort_price);
        this.nonSortColor = getResources().getColor(R.color.white);
        this.sortColor = getResources().getColor(R.color.Yellow);
        this.sortReverseColor = getResources().getColor(R.color.LawnGreen);
        // Set listeners. First update the toggles (color and value), than actually sort.
        sortByValueButton.setOnClickListener(view -> {
            toggleSortedByValue();
            SortBy();
        });
        sortByRecentButton.setOnClickListener(v -> {
            toggleSortedByRecent();
            SortBy();
        });
    }

    private void clearSearchResult() {
        resetToggleOption(); // Reload the full list
        this.searchByMode = false;
        this.editDate.setText(getString(R.string.invoice_manager_date_input_default));
        this.selectedDate = "";
        this.searchViewID.setQuery("", false);
        this.dateSearchButton.setText(R.string.invoice_manager_searchDate_button);
    }

    private void setupClearSelection() {
        ImageButton clearButton = findViewById(R.id.button_clear_selection);
        clearButton.setOnClickListener(v -> {
            if (this.RVAdapter.isSelectedEmpty())
                displaySnackBar(getString(R.string.invoice_manager_selection_fail));
            else this.RVAdapter.clearSelection();
        });
    }

    /**
     * Setup dialog for date search by user, and the search by date button.
     * This button has 2 modes: First, for filter by date. Second, clear search result.
     * */
    private void setupDatePicker() {
        this.editDate = findViewById(R.id.invoice_manager_date_textview);
        this.editDate.setText(getString(R.string.invoice_manager_date_input_default));
        this.editDate.setOnClickListener(v ->
                invoiceManagerDialog.datePickerDialog(new DialogCallback<String>() {
            @Override
            public void onSuccess(String callbackItem) {
                selectedDate = callbackItem;
                try { editDate.setText(FormatUtils.formatDateForDisplay(selectedDate)); }
                catch (ParseException e) { e.fillInStackTrace(); }
            }
            @Override
            public void onFailure(String failMSG) { }
        }));

        // The date search button with 2 modes. One for search, the other for clear.
        // Turn on mode, only if found anything, in method: 'searchByDate'.
        this.dateSearchButton = findViewById(R.id.invoice_manager_date_piker_button);
        this.dateSearchButton.setOnClickListener(view -> {
            if (this.searchByMode)
                clearSearchResult();
            else if (selectedDate.isEmpty())
                displaySnackBar(getString(R.string.invoice_manager_search_date_no_input));
            else searchByDate();
        });
    }

    /**
     * Setup the floating button for adding the new invoice. Will end this activity,
     * and start InvoiceComposingActivity.
     */
    public void setupFloatingButtonComposeInvoice() {
        FloatingActionButton actionButton = findViewById(R.id.invoice_compose_floating_button);
        actionButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, InvoiceComposingActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Setup removing button listener. When clicking on the remove option, will open an dialog
     * for erase confirmation.
     */
    private void setUpRemove() {
        ImageButton removeButton = findViewById(R.id.button_invoices_remove);
        removeButton.setOnClickListener(view -> {
            if (!this.RVAdapter.getSelectedPositions().isEmpty()) {
                String msg = getString(R.string.invoice_manager_activity_total_removing_conf,
                        this.RVAdapter.getSelectedPositions().size());
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage(msg)
                        .setPositiveButton(getString(R.string.dialog_confirm), null)
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .create();
                dialog.setOnShowListener(d -> {
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener(v -> {
                                removeSelectedInvoices(); /// Method will do the actual erasing
                                dialog.dismiss();
                            });
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                            .setOnClickListener(v -> {
                                dialog.dismiss(); /// Else continue
                            });
                });
                dialog.setIcon(R.drawable.warning_ic);
                dialog.setCancelable(false);
                dialog.show();
            }
            else displaySnackBar(getString(R.string.invoice_manager_selection_fail));
        });
    }

    private void setupEditInvoice() {
        ImageButton editButton = findViewById(R.id.button_edit_selected_invoice);
        editButton.setOnClickListener(v -> {
            List<InvoiceFrame> temp = this.RVAdapter.getSelectedFrames();
            if (temp.size() == 1) {
                InvoiceFrame selectedFrame = temp.get(0);
                if (selectedFrame != null) {
                    Intent intent = new Intent(this.getBaseContext(), InvoiceComposingActivity.class);
                    intent.putExtra("InvoiceFrameID", selectedFrame.getID());
                    startActivity(intent);
                    finish();
                }
            }
            else displaySnackBar(getString(R.string.invoices_manager_edit_fail));
        });
    }

    /**
     * Inflates the menu options in the toolbar.
     * @param menu The menu to be inflated.
     * @return True if the menu was successfully created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_invoice_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handles menu item selection.
     * @param m The selected menu item.
     * @return True if the action was handled.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem m) {
        if (m.getItemId() == R.id.home_info) {
            Toast.makeText(this, R.string.info_about_us, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (m.getItemId() == R.id.item_manager_home) {
            finish();
            return true;
        }
        if (m.getItemId() == R.id.invoice_manager_invoice_share) {
            if (this.RVAdapter.getSelectedPositions().size() == 1) {
                for (Integer single : this.RVAdapter.getSelectedPositions()) {
                    InvoiceFrame invoiceFrame = allInvoiceFrames.get(single);
                    invoiceManagerDialog.shareInvoiceFrame(invoiceFrame,
                            database.getAllRowsByID(invoiceFrame.getID(), true),
                            database.getCompany());
                }
            }
            else displaySnackBar(getString(R.string.invoices_manager_share_fail));
            return true;
        }
        if (m.getItemId() == R.id.invoice_manager_print_selected) {
            if (this.RVAdapter.getSelectedPositions().size() == 1) {
                for (Integer single : this.RVAdapter.getSelectedPositions()) {
                    InvoiceFrame invoiceFrame = allInvoiceFrames.get(single);
                    invoiceManagerDialog.printInvoiceFrame(invoiceFrame,
                            database.getAllRowsByID(invoiceFrame.getID(), true),
                            database.getCompany());
                }
            }
            else displaySnackBar(getString(R.string.invoices_manager_print_fail));
            return true;
        }
        return super.onOptionsItemSelected(m);
    }

    /** Display msg as SnackBar */
    private void displaySnackBar(@NonNull String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg,
                Snackbar.LENGTH_SHORT);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);
        snackbar.show();
    }
}
