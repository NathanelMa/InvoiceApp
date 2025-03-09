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
