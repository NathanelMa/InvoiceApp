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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InvoiceManagerDialog {

    /**
     * This class help the activity InvoiceManager:
     * 1. Display the full invoice in dialog.
     * 2. User query for remove confirmation.
     * 3. User query for edit.
     * NOTE: This class does not interaction with database.
     * NOTE: This class does not hold any list/Ref/Sync object from the activity itself,
     *       Only item list, which can be change via the ItemManger activity only.
     */

    private final List<Item> allItemsList;
    private final Context context;
    private final int ROW_TEXT_SIZE = 12;
    private final int PADDING_LEFT_RIGHT = 2;

    public InvoiceManagerDialog(@NonNull List<Item> allItemsList, Context context) {
        this.allItemsList = allItemsList;
        this.context = context;
    }

    /**
     * Display the given invoiceFrame, using data from both invoiceFrame (invoiceID, date, total),
     * and invoiceRow (Each row including: itemID, item value (single item), amount of items)
     * @param invoiceFrame InvoiceFrame 'Frame' - ID, date (dd-MM-yyyy) and total value.
     * @param invoiceRows All rows for this invoiceFrame.
     */
    public void displayInvoice(@NonNull InvoiceFrame invoiceFrame,
                               @NonNull List<InvoiceRow> invoiceRows) {
        if (invoiceFrame.isEmpty()) return;
        View dialogView = LayoutInflater.from(this.context)
                .inflate(R.layout.activity_invoice_manager_dialog_invoice, null);
        LinearLayout dialogInvoiceLayout = dialogView.findViewById(R.id.invoice_layout);

        long invoiceID = invoiceFrame.getID();
        double invoicePrice = invoiceFrame.getTotalPrice();
        String formattedDate = invoiceFrame.getDate();

        // Set header for this invoiceFrame
        TextView tvID = dialogInvoiceLayout.findViewById(R.id.table_invoice_ID);
        TextView tvTotalPrice = dialogInvoiceLayout.findViewById(R.id.table_invoice_total);
        TextView tvDate = dialogInvoiceLayout.findViewById(R.id.table_invoice_date);
        tvID.setText(FormatUtils.formatSerialNumber(invoiceID));
        tvTotalPrice.setText(FormatUtils.formatCurrency(invoicePrice));
        tvDate.setText(formattedDate); // Format date: yyyy-MM-dd hh:mm:ss

        // Setup invoiceFrame Rows
        for (InvoiceRow invoiceRow : invoiceRows) {
            LinearLayout rowLayout = createInvoiceRowLayout(
                    invoiceRow.getItemID(),
                    invoiceRow.getItemValue(),
                    invoiceRow.getQuantity(),
                    invoiceRow.getTotalRowValue());
            dialogInvoiceLayout.addView(rowLayout);
        }
        Dialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(context.getString(R.string.dialog_close), null)
                .create();
        dialog.show();
    }

    /**
     * Create text column in the layout (invoice table)
     * @param text Text present
     * @param weight Weight in the invoice table
     * @return TextView type for the invoice display.
     */
    private TextView createTextView(String text, int weight) {
        TextView textView = new TextView(this.context);
        textView.setText(text);
        textView.setLayoutParams(new LinearLayout.LayoutParams
                (0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        textView.setTextSize(ROW_TEXT_SIZE);
        textView.setPadding(2, 5, 2, 5);
        return textView;
    }

    private LinearLayout createInvoiceRowLayout(long itemID, double itemPrice,
                                                int itemQuantity, double totalRowPrice) {
        LinearLayout rowLayout = new LinearLayout(this.context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(PADDING_LEFT_RIGHT, 12, PADDING_LEFT_RIGHT, 12);

        int itemID_index = (int) itemID;
        rowLayout.addView(createTextView(String.valueOf(itemID), 1));
        rowLayout.addView(createTextView(
                String.valueOf(this.allItemsList.get(itemID_index - 1).getName()), 3));
        rowLayout.addView(createTextView(FormatUtils.formatCurrency(itemPrice), 1));
        rowLayout.addView(createTextView(String.valueOf(itemQuantity), 1));
        rowLayout.addView(createTextView(FormatUtils.formatCurrency(totalRowPrice), 1));
        return rowLayout;
    }

    public void shareInvoiceFrame(@NonNull InvoiceFrame invoiceFrame, @NonNull List<InvoiceRow> rows
            ,@NonNull CompanyDetails profile) {
        PDFGenerator.shareInvoicePdf(this.context,
                PDFGenerator.createInvoicePdf(this.context, invoiceFrame, rows, profile));
    }

    public void printInvoiceFrame(@NonNull InvoiceFrame invoiceFrame, @NonNull List<InvoiceRow> rows
            ,@NonNull CompanyDetails profile) {
        PDFGenerator.printInvoicePdf(this.context, invoiceFrame, rows, profile);
    }

    /** Start date piker dialog */
    @SuppressLint("DefaultLocale")
    public void datePickerDialog(DialogCallback<String> callback) {
        final Calendar calendarInstance = Calendar.getInstance();
        int year = calendarInstance.get(Calendar.YEAR);
        int month = calendarInstance.get(Calendar.MONTH);
        int day = calendarInstance.get(Calendar.DAY_OF_MONTH);

        // Set listener for the dialog
        DatePickerDialog.OnDateSetListener onDateSetListener
                = (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            calendarInstance.set(Calendar.YEAR, selectedYear);
            calendarInstance.set(Calendar.MONTH, selectedMonth);
            calendarInstance.set(Calendar.DAY_OF_MONTH, selectedDay);
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    selectedYear, selectedMonth + 1, selectedDay);
            callback.onSuccess(selectedDate);
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(this.context,
                android.R.style.Theme_Material_Dialog,
                onDateSetListener, year, month, day);
        Objects.requireNonNull(datePickerDialog.getWindow());
        datePickerDialog.show();
    }

    public void confirmQuery(@NonNull String queryConfirmation, DialogCallback<Object> callback) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(queryConfirmation)
                .setTitle(context.getString(R.string.dialog_title_confirmation))
                .setPositiveButton(context.getString(R.string.dialog_confirm), null)
                .setNegativeButton(context.getString(R.string.dialog_cancel), null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                callback.onSuccess(null);
                dialog.dismiss();
            });
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener(v -> {
                callback.onFailure(context.getString(R.string.dialog_canceled_by_user));
                dialog.dismiss();
            });
        });
        dialog.show();
    }
}
