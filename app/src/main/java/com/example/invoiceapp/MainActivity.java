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

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;

public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.home_info) {
            Toast.makeText(this, R.string.info_about_us, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        animation_image();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle(getString(R.string.home_title));
        animation_image();

        Button buttonItems = findViewById(R.id.home_items_button);
        Button buttonInvoices = findViewById(R.id.home_invoices_button);
        Button buttonStatistics = findViewById(R.id.home_statistics_button);
        Button buttonEditProfile = findViewById(R.id.profile_edit_button);
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        if (databaseHelper.getCompany() == null) {
            Intent intent = new Intent(this, CompanyActivity.class);
            intent.putExtra("isEditMode","false");
            startActivity(intent);
            finish();
        }

        buttonItems.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), ItemsActivity.class);
            startActivity(intent);
        });

        buttonInvoices.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), InvoiceManagerActivity.class);
            startActivity(intent);
        });

        buttonStatistics.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), StatisticsActivity.class);
            startActivity(intent);
        });

        buttonEditProfile.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), CompanyActivity.class);
            intent.putExtra("isEditMode","true");
            startActivity(intent);
        });
    }

    private void animation_image() {
        ImageView svgImageView = findViewById(R.id.image);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                svgImageView, "alpha", 0f, 1f);
        fadeIn.setDuration(2500);
        fadeIn.start();
    }
}

/*
        this.buttonItems.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), ItemsActivity.class);
            startActivity(intent);
        });

        this.buttonInvoices.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), InvoiceManagerActivity.class);
            startActivity(intent);
        });
        this.buttonEditProfile.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.super.getBaseContext(), CompanyDetailsActivity.class);
            intent.putExtra("isEditMode","true");
            startActivity(intent);
        });
 */