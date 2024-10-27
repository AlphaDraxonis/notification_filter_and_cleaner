/*
 * Notification Filter
 * Copyright (C) 2024-2024 AlphaDraxonis
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

package com.alphadraxonis.notificationcleaner.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.alphadraxonis.notificationcleaner.MainActivity;
import com.alphadraxonis.notificationcleaner.R;
import com.alphadraxonis.notificationcleaner.filters.DefaultFilterRule;
import com.alphadraxonis.notificationcleaner.filters.FilterRule;

public class FilterUiList extends RecyclerView.Adapter<FilterUiList.Holder> {

    private final MainActivity mainActivity;
    private final PackageManager packageManager;

    public FilterRule[] filters;
    final Set<FilterRule> selectedFilters = new HashSet<>();
    private final Set<Holder> selectedHolders = new HashSet<>();

    public FilterUiList(Collection<FilterRule> filtersToShow, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        packageManager = mainActivity.getPackageManager();

        filters = new FilterRule[filtersToShow.size()];
        int i = 0;
        for (FilterRule obj : filtersToShow) {
            filters[i++] = obj;
        }

        Arrays.sort(filters);
    }

    public Set<FilterRule> getSelectedObjects() {
        return selectedFilters;
    }

    public void resetSelection() {
        selectedFilters.clear();
        for (Holder viewHolder : selectedHolders) {
            viewHolder.updateSelectionState();
        }
        mainActivity.hideBottomToolbar();
    }


    public class Holder extends RecyclerView.ViewHolder {
        protected final View view;
        protected ImageView appIcon;
        protected TextView appLabel;
        protected TextView searchTerm;
        protected SwitchCompat switchEnable;

        protected FilterRule obj;

        protected Holder(View v) {
            this(v, false);
        }

        public Holder(View v, boolean emptyView) {
            super(v);
            view = v;

            initComponents(emptyView);
        }

        protected void initComponents(boolean emptyView) {
            if (!emptyView) {
                appIcon = itemView.findViewById(R.id.app_icon);
                appLabel = itemView.findViewById(R.id.app_label);
                searchTerm = itemView.findViewById(R.id.search_term);
                switchEnable = itemView.findViewById(R.id.switch_enable);
            }
        }

        public void update(FilterRule obj) {
            if (obj == null) {
                return;
            }
            this.obj = obj;


            DefaultFilterRule filterRule = (DefaultFilterRule) obj;

            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(filterRule.targetAppPackage, 0);
                appIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo));
                appLabel.setText(packageManager.getApplicationLabel(appInfo));
            } catch (PackageManager.NameNotFoundException ex) {
//                appIcon.setImageDrawable(AppCompatResources.getDrawable(mainActivity, R.drawable.warning_30));
                appIcon.setImageDrawable(null);
                appLabel.setText(filterRule.targetAppPackage);
            }

            String mode = filterRule.mode.displayName + " ";
            SpannableString spannableString = new SpannableString(mode + filterRule.query);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD_ITALIC),
                    mode.length(),
                    spannableString.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            searchTerm.setText(spannableString);


            switchEnable.setChecked(filterRule.isEnabled());

            updateSelectionState();
        }

        public FilterRule getFilterRule() {
            return obj;
        }

        public void updateSelectionState() {
            if (selectedFilters.contains(getFilterRule())) {
                selectedHolders.add(this);
                itemView.setBackgroundColor(mainActivity.getResources().getColor(R.color.selected_background));
                mainActivity.showBottomToolbar();
            } else {
                selectedHolders.remove(this);
                TypedValue outValue = new TypedValue();
                mainActivity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                itemView.setBackgroundResource(outValue.resourceId);
                if (selectedFilters.isEmpty()) mainActivity.hideBottomToolbar();
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (filters.length == 0) {
            holder.update(null);
            selectedHolders.remove(holder);
            return;
        }
        FilterRule obj = filters[position];
        holder.update(obj);

        holder.view.setOnClickListener(v -> {
            if (selectedFilters.contains(obj)) {
                selectedFilters.remove(obj);
                holder.updateSelectionState();
            } else if (selectedFilters.isEmpty()) {
                normalClickAction(position);
            } else {
                selectedFilters.add(obj);
                holder.updateSelectionState();
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (selectedFilters.contains(obj)) return false;
            selectedFilters.add(obj);
            holder.updateSelectionState();
            return true;
        });

        if (holder.switchEnable != null) {
            holder.switchEnable.setOnCheckedChangeListener((v, isChecked) -> {
                obj.setEnabled(isChecked);
            });
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == R.layout.no_filters) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.no_filters, parent, false);
            return new Holder(v, true);
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_in_list, parent, false);
        return new Holder(v);
    }

    protected void normalClickAction(int position) {
        new EditFilterDialog(mainActivity, (DefaultFilterRule) filters[position], filterRule -> {
            if (filterRule != null) {
                resetSelection();
                mainActivity.showFilters(mainActivity.filters);
                mainActivity.scrollToElement(filterRule);
            }
        }).getDialog().show();
    }

    @Override
    public int getItemCount() {
        return Math.max(filters.length, 1);
    }

    public int getItemViewType(int position) {
        return filters.length == 0 ? R.layout.no_filters : R.layout.filter_in_list;
    }

}