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

package com.alphadraxonis.notificationcleaner;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.alphadraxonis.notificationcleaner.filters.FilterRule;
import com.alphadraxonis.notificationcleaner.ui.DeleteFilterDialog;
import com.alphadraxonis.notificationcleaner.ui.EditFilterDialog;
import com.alphadraxonis.notificationcleaner.ui.FilterUiList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private FilterUiList filterUiList;

    private FloatingActionButton addBtn;

    private LinearLayout bottomToolbar;
    private LinearLayout[] toolbarButtons;
    private LinearLayout deleteBtn;

    private static int scrollPosItem, scrollPos;

    public Set<FilterRule> filters;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomToolbar = findViewById(R.id.bottomToolbar);
        toolbarButtons = new LinearLayout[]{
                deleteBtn = findViewById(R.id.delete)
        };

        deleteBtn.setOnClickListener(v -> {
            new DeleteFilterDialog(this, filterUiList.getSelectedObjects()).confirmFileDeletion();
        });

        addBtn = findViewById(R.id.floating_add_btn);
        addBtn.setOnClickListener(v -> {
            new EditFilterDialog(this, null, filterRule -> {
                if (filterRule != null) {
                    filters.add(filterRule);
                    showFilters(filters);
                    scrollToElement(filterRule);
                }
            }).getDialog().show();
        });

        filters = FileUtils.loadFilterRules(this);

        showFilters(filters);


        ViewTreeObserver toolbarObserver = bottomToolbar.getViewTreeObserver();
        toolbarObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (bottomToolbar.getVisibility() == View.VISIBLE) {
                    updateLayoutParameterForBottomToolbar(true);
                    bottomToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);//only need to listen for first layout
                }
            }
        });

        bottomToolbar.setVisibility(View.GONE);



        if (!NotificationService.hasNotificationAccess(this)) {
            NotificationService.requestNotificationAccess(this);
        } else {
            NotificationService.startService(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NotificationService.hasNotificationAccess(this)) {
            NotificationService.startService(this);
        }
    }

    @Override
    protected void onPause() {
        applyChanges();
        super.onPause();
    }

    @Override
    protected void onStop() {
        applyChanges();
        super.onStop();
    }

    public void applyChanges() {

        try {
            FileUtils.saveFilterRules(this, filters);
        } catch (IOException e) {
            new AlertDialog.Builder(this)
                    .setTitle(e.getClass().getSimpleName())
                    .setMessage(e.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }

        NotificationService.needsReload = true;
    }


    public void showFilters(Collection<FilterRule> filters) {
        if (recyclerView.getLayoutManager() != null) {
            int currentPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            View currentView = recyclerView.getChildAt(0);//first visible child
            if (currentView != null) {
                scrollPosItem = currentPosition;
                scrollPos = -currentView.getTop();
            }
        }

        updateFiltersView(filters, false);

        recyclerView.scrollToPosition(scrollPosItem);
        recyclerView.scrollBy(0, scrollPos - recyclerView.getPaddingTop());
    }

    private void updateFiltersView(Collection<FilterRule> filters, boolean saveScrollPos) {
        if (saveScrollPos && recyclerView.getLayoutManager() != null) {
            int currentPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            View currentView = recyclerView.getChildAt(0);//first visible child
            if (currentView != null) {
                scrollPosItem = currentPosition;
                scrollPos = -currentView.getTop();
            }
        }
        filterUiList = new FilterUiList(filters, this);
        recyclerView.setAdapter(filterUiList);
        if (saveScrollPos) {
            recyclerView.scrollToPosition(scrollPosItem);
            recyclerView.scrollBy(0, scrollPos - recyclerView.getPaddingTop());
        }
    }

    public void scrollToElement(FilterRule element) {
        int index = 0;
        for (Object obj : filterUiList.filters) {
            if (obj == element) break;
            index++;
        }
        recyclerView.scrollToPosition(index);
    }


    private long backPressed;

    @Override
    public void onBackPressed() {
        if (backPressed + 3000 > System.currentTimeMillis()) {
            super.onBackPressed();
        }
        else {
            backPressed = System.currentTimeMillis();
            Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_LONG).show();
        }
    }


    public void showBottomToolbar() {

        if (bottomToolbar.getVisibility() != View.VISIBLE) {

            bottomToolbar.setVisibility(View.VISIBLE);
            updateLayoutParameterForBottomToolbar(true);

            ValueAnimator rotateInAnimator = ValueAnimator.ofFloat(90f, 0f);
            rotateInAnimator.setDuration(300);
            rotateInAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();

                for (LinearLayout btn : toolbarButtons) {
                    btn.setRotationX(value);
                }
            });
            rotateInAnimator.start();
        }
    }

    public void hideBottomToolbar() {
        if (bottomToolbar.getVisibility() != View.GONE) {
            bottomToolbar.setVisibility(View.GONE);
            updateLayoutParameterForBottomToolbar(false);
        }
    }

    private void updateLayoutParameterForBottomToolbar(boolean visible) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) recyclerView).getLayoutParams();
        params.bottomMargin = visible ? bottomToolbar.getHeight() : 0;
        recyclerView.setLayoutParams(params);
        params = (CoordinatorLayout.LayoutParams) addBtn.getLayoutParams();
        params.bottomMargin = (visible ? bottomToolbar.getHeight() : 0) + addBtn.getPaddingBottom();
        addBtn.setLayoutParams(params);
    }

}