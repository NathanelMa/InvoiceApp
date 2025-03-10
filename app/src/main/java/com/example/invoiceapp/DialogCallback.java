package com.example.invoiceapp;

/*
 * Copyright (c) 2025 NathanEl Mark, Dor Binyamin, Orel Gigi
 *
 * This software is licensed under the MIT License.
 * See the LICENSE file in the root directory for details.
 */

/**
 * Interface for handling the result of dialog actions.
 * Provides callbacks for success and failure scenarios.
 * The dialog helpers classes doesn't required to implement it:
 * Target activity must implement it and with the call for the dialog, type the DialogCallback
 * as arguments.
 */
public interface DialogCallback<T> {

    /**
     * Success callback. This method will be called when the dialog success,
     * The method received the T type as callback type, from the calling side.
     */
    void onSuccess(T callbackItem);

    /**
     * On fail callback. Will contain the msg and dismiss the dialog.
     * @param failMSG String for msg contain the reason for failing.
     */
    void onFailure(String failMSG);
}
