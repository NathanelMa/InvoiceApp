package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class FormatUtils {

    private static NumberFormat integerFormat = null; // Format example: 1,000.00 ; 1.000.00
    private static NumberFormat serialFormat = null; // 10 zero fill
    private static NumberFormat currencyFormat = null; // Max fraction digits: 2
    private static SimpleDateFormat dateFormatFull = null; // yyyy-MM-dd HH:mm:ss
    private static SimpleDateFormat targetFormat = null;
    private static SimpleDateFormat dateFormatCompress = null; // dd-MM-yyyy
    private static SimpleDateFormat inputUserDateFormat = null; // yyyy-MM-dd
    private static SimpleDateFormat outputFormatDateFormat = null; // dd/MM/yyyy

    private static boolean init = false;

    private FormatUtils() { }

    private static void initAll() {
        integerFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
        serialFormat = NumberFormat.getInstance(Locale.getDefault());
        serialFormat = new DecimalFormat("0000000000");
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormat.setMaximumFractionDigits(2);
        dateFormatFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormatCompress = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        targetFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        inputUserDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        outputFormatDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        init = true;
    }

    public static String formatInteger(int number) {
        if (!init) initAll();
        return integerFormat.format(number);
    }

    public static String formatSerialNumber(double number) {
        if (!init) initAll();
        return "#" + serialFormat.format(number);
    }

    public static String formatCurrency(double amount) {
        if (!init) initAll();
        return currencyFormat.format(amount);
    }

    public static String formatDateFull(String dateString) throws ParseException {
        if (!init) initAll();
        return formatDate(dateString, dateFormatCompress, dateFormatFull);
    }

    public static String formatDateCompressed(String dateString) throws ParseException{
        if (!init) initAll();
        return formatDate(dateString, dateFormatFull, dateFormatCompress);
    }

    public static String changeFullDateFormat(String dateString) throws ParseException {
        Date date = dateFormatFull.parse(dateString);
        assert date != null;
        return targetFormat.format(date);
    }

    // String compressedDate = FormatUtils.formatDateCompressed("2025-02-18 12:30:45");
    // "18-02-2025"

    // String fullDate = FormatUtils.formatDateFull("18-02-2025");
    // "2025-02-18 00:00:00"

    private static String formatDate(String dateString, SimpleDateFormat inputFormat,
                                     SimpleDateFormat outputFormat) throws ParseException {
        Date date = inputFormat.parse(dateString);
        assert date != null;
        return outputFormat.format(date);
    }

    public static String formatDateForDisplay(String date) throws ParseException {
        Date parsedDate = inputUserDateFormat.parse(date);
        assert parsedDate != null;
        return outputFormatDateFormat.format(parsedDate);
    }

    public static String getCurrentTimestamp() {
        return dateFormatFull.format(new Date());
    }
}
