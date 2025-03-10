package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.widget.Toast;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import androidx.core.content.FileProvider;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import com.itextpdf.kernel.pdf.PdfDocument;

public class PDFGenerator {

    public static File createInvoicePdf(Context context, InvoiceFrame invoiceFrame,
                                        List<InvoiceRow> rows, CompanyDetails profile) {
        try {

            // Create directory for the PDF if it doesn't exist
            File pdfDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            // Define the file path for the PDF
            File pdfFile = new File(pdfDir, "Invoice_" + invoiceFrame.getID() + ".pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Add company name and contact info at the top
            document.add(new Paragraph(profile.toString())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));

            // Add invoiceFrame details header
            document.add(new Paragraph("Account Breakdown")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(14));

            // Create a table for the invoiceFrame details
            Table table = new Table(4); // 4 columns for Description, Amount, Price, and Total
            table.setWidth(100); // Make the table span the full width

            // Add headers to the table
            table.addCell(new Cell().add(new Paragraph("Description"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());
            table.addCell(new Cell().add(new Paragraph("Amount"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());
            table.addCell(new Cell().add(new Paragraph("Price"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());
            table.addCell(new Cell().add(new Paragraph("Total"))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());

            double total = 0;

            // Loop through each row in the invoiceFrame and add to the table
            for (InvoiceRow row : rows) {
                // Add description, amount, price, and total to the table cells
                table.addCell(new Cell().add(new Paragraph(row.getItemName()))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(row.getQuantity())))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", row.getItemValue())))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", row.getTotalRowValue())))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            total = invoiceFrame.getTotalPrice();
            // Center the table in the document
            document.add(new Paragraph().add(table).setTextAlignment(TextAlignment.CENTER));

            // Add total invoiceFrame amount, centered
            document.add(new Paragraph("\nTotal Account: " + String.format("%.2f", total))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(14));

            // Add service note
            document.add(new Paragraph("\nSERVICE NOT INCLUDED :)....")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(12));

            // Close the document after adding all content
            document.close();

            // Return the generated PDF file
            return pdfFile;

        } catch (Exception e) {
            // Handle any exceptions that occur during PDF creation
            e.printStackTrace();
            return null;
        }
    }

    public static void printInvoicePdf(Context context, InvoiceFrame invoice,
                                       List<InvoiceRow> rows, CompanyDetails profile) {
        // Generate the invoice PDF file.
        File pdfFile = PDFGenerator.createInvoicePdf(context, invoice, rows, profile);

        if (pdfFile == null) {
            Toast.makeText(context, "Print Error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the PrintManager service to handle printing.
        @SuppressLint("ServiceCast") android.print.PrintManager printManager =
                (android.print.PrintManager) context.getSystemService(Context.PRINT_SERVICE);

        // Set print attributes such as page size, color mode, and resolution.
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .setResolution(new PrintAttributes.Resolution("pdf", "PDF", 600, 600))
                .build();

        // Create a PrintDocumentAdapter to handle the printing process.
        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal, LayoutResultCallback callback,
                                 Bundle extras) {
                // Define the document information, including its name and page count.
                callback.onLayoutFinished(new PrintDocumentInfo.Builder("Invoice_" + invoice.getID() + ".pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1) // Assuming one-page invoice.
                        .build(), true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal, WriteResultCallback callback) {
                try (FileInputStream inputStream = new FileInputStream(pdfFile);
                     FileOutputStream outputStream = new FileOutputStream(destination.getFileDescriptor())) {

                    // Copy data from the generated PDF file to the print destination.
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    // Notify the system that printing is complete.
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                } catch (Exception e) {
                    callback.onWriteFailed(e.toString()); // Handle write failures.
                }
            }
        };

        // Start the printing process.
        printManager.print("Invoice_" + invoice.getID(), printAdapter, printAttributes);
        Toast.makeText(context, context.getString(R.string.pdf_generator_print), Toast.LENGTH_SHORT).show();
    }

    public static void shareInvoicePdf(Context context, File pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "No file to share", Toast.LENGTH_SHORT).show();
            return;
        }
        // Generate a content URI for the PDF file using FileProvider.
        Uri pdfUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);

        // Create an intent to share the invoice PDF via compatible apps.
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice #" + pdfFile.getName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is the invoice document.");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Grant temporary read permission.

        // Launch the sharing chooser.
        context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"));
    }
}