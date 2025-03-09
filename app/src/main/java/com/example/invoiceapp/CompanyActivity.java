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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Objects;

public class CompanyActivity extends AppCompatActivity {

    private EditText editTextCompanyName, editTextCompanyPhone, editTextCompanyId,
            editTextCompanyLocation;
    private Button buttonSave, buttonCancel;
    private boolean isEditMode;
    private DatabaseHelper databaseHelper;
    private CompanyDetails companyDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(getString(R.string.profile_edit_title));
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_company);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        this.editTextCompanyPhone = findViewById(R.id.editTextCompanyPhone);
        this.editTextCompanyId = findViewById(R.id.editTextCompanyId);
        this.editTextCompanyLocation = findViewById(R.id.editTextCompanyLocation);
        this.editTextCompanyName = findViewById(R.id.editTextCompanyName);
        this.buttonCancel = findViewById(R.id.btnCencel);
        this.buttonSave = findViewById(R.id.btnSave);
        isEditMode = Objects.equals(getIntent().getStringExtra("isEditMode"), "true");

        if (isEditMode)
            loadCompanyData();
        else
            buttonCancel.setVisibility(View.GONE);

        // Save button listener
        buttonSave.setOnClickListener(view -> saveCompanyData());

        // Cancel button listener
        buttonCancel.setOnClickListener(view -> finish());
    }

    /** Suppress BackPressed from user */
    @SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        // Suppress back call from user
        if (isEditMode) {
            Intent intent = new Intent(CompanyActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else finish();
    }

    private void loadCompanyData(){
        this.buttonCancel.setVisibility(View.VISIBLE);
        CompanyDetails company = databaseHelper.getCompany();
        if (company != null) {
            editTextCompanyName.setText(company.getCompanyName());
            editTextCompanyPhone.setText(company.getCompanyNumber());
            editTextCompanyId.setText(company.getCompanyId());
            editTextCompanyLocation.setText(company.getCompanyAddress());
        }
        else {
            Toast.makeText(this, getString(R.string.error_action_data_base),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCompanyData() {
        String companyName = editTextCompanyName.getText().toString().trim();
        String companyPhone = editTextCompanyPhone.getText().toString().trim();
        String companyId = editTextCompanyId.getText().toString().trim();
        String companyLocation = editTextCompanyLocation.getText().toString().trim();

        // Input validation
        if (companyName.isEmpty()) {
            editTextCompanyName.setError(getString(R.string.edit_profile_invalid_name));
            return;
        }
        if (companyPhone.isEmpty() || companyPhone.length() < 9) {
            editTextCompanyPhone.setError(getString(R.string.edit_profile_invalid_phone));
            return;
        }
        if (companyId.isEmpty() || companyId.length() < 8) {
            editTextCompanyId.setError(getString(R.string.edit_profile_invalid_id));
            return;
        }
        if (companyLocation.isEmpty()) {
            editTextCompanyLocation.setError(getString(R.string.edit_profile_invalid_location));
            return;
        }

        // Create company object
        CompanyDetails company = new CompanyDetails(companyName,companyLocation, companyPhone, companyId);

        if (isEditMode) {
            databaseHelper.UpdateCompany(company);
            Toast.makeText(this, getString(R.string.edit_profile_updated_successfully),
                    Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.UpdateCompany(company);
            Toast.makeText(this, getString(R.string.edit_profile_new_successfully),
                    Toast.LENGTH_SHORT).show();
        }

        // Navigate back to MainActivity
        Intent intent = new Intent(CompanyActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the current activity
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
}