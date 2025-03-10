package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

import androidx.annotation.NonNull;

public class CompanyDetails {

    private String companyName;
    private String companyAddress;
    private String companyNumber;
    private String companyId;

    public CompanyDetails(String companyName, String companyAddress, String companyNumber, String companyId) {
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.companyNumber = companyNumber;
        this.companyId = companyId;
    }

    public String getCompanyName() {return companyName;}

    public void setCompanyName(String companyName) {this.companyName = companyName;}

    public String getCompanyAddress() {return companyAddress;}

    public void setCompanyAddress(String companyAddress) {this.companyAddress = companyAddress;}

    public String getCompanyNumber() {return companyNumber;}

    public void setCompanyNumber(String companyNumber) {this.companyNumber = companyNumber;}

    public String getCompanyId() {return companyId;}

    public void setCompanyId(String companyId) {this.companyId = companyId;}

    @NonNull
    @Override
    public String toString() {
        return  companyName + '\n'
                + companyId + '\n'
                +"Tel:"+  companyNumber + '\n'
                + companyAddress
                + '\n';
    }
}
