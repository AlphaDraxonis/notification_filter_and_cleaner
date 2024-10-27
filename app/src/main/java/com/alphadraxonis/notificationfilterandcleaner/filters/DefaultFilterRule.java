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

package com.alphadraxonis.notificationfilterandcleaner.filters;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;

import java.util.Objects;

public class DefaultFilterRule extends FilterRule {

    public String targetAppPackage;
    public String query;
    public StringComparisonMode mode = StringComparisonMode.CONTAINS;

    @Override
    public boolean accept(StatusBarNotification notification) {
        String appPackage = notification.getPackageName();
        return appPackage.equals(targetAppPackage)
                && mode.accept(query, notification.getNotification().extras.getString(Notification.EXTRA_TEXT));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;

        if (mode != ((DefaultFilterRule) obj).mode) return false;
        if (!Objects.equals(targetAppPackage, ((DefaultFilterRule) obj).targetAppPackage)) return false;
        if (!Objects.equals(query, ((DefaultFilterRule) obj).query)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetAppPackage, query, mode);
    }

    @Override
    public int compareTo(FilterRule o) {
        if (o instanceof DefaultFilterRule) {
            return targetAppPackage.compareTo(((DefaultFilterRule) o).targetAppPackage) * 1000 + query.compareTo(((DefaultFilterRule) o).query);
        }
        return 0;
    }

    private static final String TARGET_APP_PACKAGE = "target_app_package";
    private static final String QUERY = "query";
    private static final String MODE = "mode";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.putString(TARGET_APP_PACKAGE, targetAppPackage);
        bundle.putString(QUERY, query);
        bundle.putString(MODE, mode.name());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        targetAppPackage = bundle.getString(TARGET_APP_PACKAGE);
        query = bundle.getString(QUERY);
        mode = StringComparisonMode.valueOf(bundle.getString(MODE));
    }
}