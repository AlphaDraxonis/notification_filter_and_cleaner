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

package com.alphadraxonis.notificationfilterandcleaner.filters;

public enum StringComparisonMode {

    //don't change code name for bundling!
    CONTAINS("contains"),
    CONTAINS_IGNORE_CASE("contains (ignore case)"),
    STARTS_WITH("starts with"),
    STARTS_WITH_IGNORE_CASE("starts with (ignore case)"),
    ENDS_WITH("ends with"),
    ENDS_WITH_IGNORE_CASE("ends with (ignore case)");

    public final String displayName;

    StringComparisonMode(String displayName) {
        this.displayName = displayName;
    }

    public boolean accept(String query, String fullString) {
        if (fullString == null || query == null) return false;
        switch (this) {
            case CONTAINS: return fullString.contains(query);
            case CONTAINS_IGNORE_CASE: return fullString.toLowerCase().contains(query.toLowerCase());
            case STARTS_WITH: return fullString.startsWith(query);
            case STARTS_WITH_IGNORE_CASE: return fullString.toLowerCase().startsWith(query.toLowerCase());
            case ENDS_WITH: return fullString.endsWith(query);
            case ENDS_WITH_IGNORE_CASE: return fullString.toLowerCase().endsWith(query.toLowerCase());
        }
        return false;
    }
}