/*
 * Notification Filter and Cleaner
 * Copyright (C) 2024 AlphaDraxonis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.alphadraxonis.notificationfilterandcleaner.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import com.alphadraxonis.notificationfilterandcleaner.R;
import com.alphadraxonis.notificationfilterandcleaner.filters.DefaultFilterRule;
import com.alphadraxonis.notificationfilterandcleaner.filters.FilterRule;

public final class EditFilterDialog {

    private final @NonNull Context context;
    private final View view;
    private final Consumer<FilterRule> doWhenDone;

    private final AlertDialog dialog;
    private final Button cancelButton, confirmButton;

    private final EditText appPackageInput, queryInput;

    private DefaultFilterRule filterToEdit;


    public EditFilterDialog(@NonNull Context context, DefaultFilterRule filterToEdit, Consumer<FilterRule> doWhenDone) {
        this.context = context;
        this.filterToEdit = filterToEdit;
        this.doWhenDone = doWhenDone;

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        view = LayoutInflater.from(context).inflate(R.layout.edit_filter_dialog, null);

        appPackageInput = view.findViewById(R.id.package_name_input);
        queryInput = view.findViewById(R.id.query_input);

        cancelButton = view.findViewById(R.id.btn_cancel);
        confirmButton = view.findViewById(R.id.btn_okay);

        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        if (filterToEdit != null) {
            appPackageInput.setText(filterToEdit.targetAppPackage);
            queryInput.setText(filterToEdit.query);
        }

        cancelButton.setOnClickListener(v -> {
            endDialog(null);
        });
        confirmButton.setOnClickListener(v -> {
            if (this.filterToEdit == null) this.filterToEdit = new DefaultFilterRule();
            this.filterToEdit.targetAppPackage = appPackageInput.getText().toString().trim();
            this.filterToEdit.query = queryInput.getText().toString();

            endDialog(this.filterToEdit);
        });

    }

    private void endDialog(FilterRule result) {
        if (doWhenDone != null) doWhenDone.accept(result);
        dialog.dismiss();
    }

    public AlertDialog getDialog() {
        return dialog;
    }

}