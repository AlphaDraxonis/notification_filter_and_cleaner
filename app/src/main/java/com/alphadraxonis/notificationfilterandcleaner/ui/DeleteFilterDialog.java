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

import android.app.AlertDialog;
import android.os.Handler;

import java.util.Set;

import com.alphadraxonis.notificationfilterandcleaner.MainActivity;
import com.alphadraxonis.notificationfilterandcleaner.R;
import com.alphadraxonis.notificationfilterandcleaner.filters.FilterRule;

public class DeleteFilterDialog {
    private MainActivity context;
    private Set<FilterRule> selectedFilterRules;

    public DeleteFilterDialog(MainActivity context, Set<FilterRule> selectedFilterRules) {
        this.context = context;
        this.selectedFilterRules = selectedFilterRules;
    }

    public void confirmFileDeletion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.delete));
        builder.setMessage(context.getString(R.string.delete_msg));
        builder.setPositiveButton(context.getString(R.string.delete), (dialog, which) -> deleteSelectedFiles());
        builder.setNegativeButton(context.getString(android.R.string.cancel), null);
        builder.show();
    }

    private void deleteSelectedFiles() {
        for (FilterRule filterRule : selectedFilterRules) {
            context.filters.remove(filterRule);
        }
        selectedFilterRules.clear();

        context.showFilters(context.filters);

        context.hideBottomToolbar();

        new Handler().post(() -> context.applyChanges() );
    }
}