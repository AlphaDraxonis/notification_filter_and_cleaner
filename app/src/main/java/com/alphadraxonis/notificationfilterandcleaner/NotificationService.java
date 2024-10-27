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

package com.alphadraxonis.notificationfilterandcleaner;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationManagerCompat;

import java.util.Set;

import com.alphadraxonis.notificationfilterandcleaner.filters.FilterRule;

public class NotificationService extends NotificationListenerService {

    private Set<FilterRule> filterRules;

    static boolean needsReload = false;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

        filterRules = FileUtils.loadFilterRules(this);
        needsReload = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

        if (filterRules == null) {
            return;
        }

        if (needsReload) {
            filterRules = FileUtils.loadFilterRules(this);
            needsReload = false;
        }

        for (FilterRule filterRule : filterRules) {
            if (filterRule.isEnabled() && filterRule.accept(statusBarNotification)) {
                new Handler().postDelayed(() -> cancelNotification(statusBarNotification.getKey()), 10);
                break;
            }
        }
    }


    static boolean hasNotificationAccess(Context context) {
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName());
    }

    static void requestNotificationAccess(Context context) {
        context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    static void startService(Context context) {
        context.startService(new Intent(context, NotificationService.class));
    }

}