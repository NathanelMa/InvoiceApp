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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Calendar;


public class StatisticsActivity extends AppCompatActivity {

    private TextView totalRevenueTextView;
    private TextView bestSellingItemTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.statistics_title));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Bind UI elements
        totalRevenueTextView = findViewById(R.id.total_revenue_value);
        bestSellingItemTextView = findViewById(R.id.best_selling_value);
        loadStatistics();
    }

    @SuppressLint("SetTextI18n")
    private void loadStatistics() {
        DatabaseHelper database = DatabaseHelper.getInstance(getApplicationContext());
        double[] revenues = database.getTotalRevenues();
        String bestSellingItem = database.bestSellingItem();
        String allRevs = getRevenuesString(revenues);
        double thisRev = getCurrentMonthRevenue(revenues);

        totalRevenueTextView.setText(allRevs);
        bestSellingItemTextView.setText(bestSellingItem);

        TextView dateInfoTextView = findViewById(R.id.date_info);
        TextView thisRevTextView = findViewById(R.id.total_revenue_label);
        thisRevTextView.setText(thisRevTextView.getText() + " " + FormatUtils.formatCurrency(thisRev));

        ProgressBar progressBar = findViewById(R.id.progressBar);
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysLeft = totalDaysInMonth - currentDay;
        progressBar.setMax(totalDaysInMonth);
        progressBar.setProgress(currentDay);

        @SuppressLint("DefaultLocale") String currentDate = String.format("%02d/%02d/%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));

        String dateInfoText = currentDate + " | " + daysLeft;
        dateInfoTextView.setText(dateInfoText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_basic, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.home_info) {
            Toast.makeText(this,getString(R.string.info_about_us), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static String getRevenuesString(double[] revs) {
        String[] months = {
                "January: ", "February: ", "March: ", "April: ", "May: ", "June: ",
                "July: ", "August: ", "September: ", "October: ", "November: ", "December: "
        };
        if (revs == null || revs.length != 12) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            String rev = FormatUtils.formatCurrency(revs[i]);
            result.append(months[i]).append(rev);
            if (i + 1 < 12) result.append("\n");
        }
        return result.toString();
    }

    private static double getCurrentMonthRevenue(double[] revs) {
        if (revs == null || revs.length != 12) return 0;
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        return revs[currentMonth];
    }
}