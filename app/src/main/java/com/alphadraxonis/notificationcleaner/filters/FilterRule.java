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

package com.alphadraxonis.notificationcleaner.filters;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;

public abstract class FilterRule implements Comparable<FilterRule> {

    public abstract boolean accept(StatusBarNotification notification);

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static final String ENABLED = "enabled";

    public void storeInBundle(Bundle bundle) {
        bundle.putBoolean(ENABLED, enabled);
    }

    public void restoreFromBundle(Bundle bundle) {
        enabled = bundle.getBoolean(ENABLED);
    }

}