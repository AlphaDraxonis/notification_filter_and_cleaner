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

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.alphadraxonis.notificationfilterandcleaner.filters.DefaultFilterRule;
import com.alphadraxonis.notificationfilterandcleaner.filters.FilterRule;
import com.alphadraxonis.notificationfilterandcleaner.filters.StringComparisonMode;

public final class FileUtils {

    private FileUtils() {}

    private static final String FILTERS_DIR = "filters";
    private static final String FILTERS_DIR_TMP = FILTERS_DIR + "_tmp";


    public static void saveFilterRules(Context context, Collection<FilterRule> filterRules) throws IOException {

        String errors = null;

        File mainFilterDir = new File(context.getExternalFilesDir(""), FILTERS_DIR);
        File tmpFilterDir = new File(context.getExternalFilesDir(""), FILTERS_DIR_TMP);

        if (tmpFilterDir.exists()) {
            if (tmpFilterDir.isFile()) {
                throw new IOException("Temp-Directory is a file, not a directory!!!");
            }
            else errors = appendError(errors, "Temp-Directory wasn't cleared before starting saving. This might cause issues, but trying to save anyways.");
        }

        if (!tmpFilterDir.mkdir()) {
            errors = appendError(errors, "Unable to initialize Temp-Directory! Maybe the directory already exists. Trying to save anyways.");
        }

        Set<String> usedFileNames = new HashSet<>();

        for (FilterRule filterRule : filterRules) {
            Bundle bundle = new Bundle();
            filterRule.storeInBundle(bundle);

            final String originalFilename = String.valueOf(filterRule.hashCode());
            String fileName = originalFilename;
            if (usedFileNames.contains(fileName)) {
                int i = 1;
                do {
                    fileName = originalFilename + " (" + i + ")";
                } while (usedFileNames.contains(fileName));
            }

            try {
                File file = new File(context.getExternalFilesDir(""), FILTERS_DIR_TMP + "/" + fileName);

                if (file.exists()) file.delete();//shouldn't exist anyways

                if (!file.createNewFile()) {
                    errors = appendError(errors, "Could not create a file: " + FILTERS_DIR_TMP + "/" + fileName);
                }

                FileOutputStream out = new FileOutputStream(file, false);

                Parcel parcel = Parcel.obtain();
                bundle.writeToParcel(parcel, 0);
                byte[] bytes = parcel.marshall();
                parcel.recycle();

                out.write(bytes);

                out.flush();
                out.close();

                usedFileNames.add(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                errors = appendError(errors, e.getClass().getSimpleName() + " while saving a file: " + e.getMessage());
            }
        }

        if (errors != null) {
            errors = appendError(errors, "\nSaving wasn't successful!");
            throw new IOException(errors);
        }

        File[] oldFiles = mainFilterDir.listFiles();
        if (oldFiles != null) {
            for (int i = 0; i < oldFiles.length; i++) {
                if (!oldFiles[i].delete()) {
                    errors = appendError(errors, "Error while replacing an existing file: " + oldFiles[i].getName());
                }
            }
        }
        mainFilterDir.delete();
        if (!tmpFilterDir.renameTo(mainFilterDir)) {
            errors = appendError(errors, "Error while moving the Temp-Directory to the real directory!");
        }

        if (errors != null) {
            errors = appendError(errors, "\nCritical problems occured during saving!");
            throw new IOException(errors);
        }
    }

    public static Set<FilterRule> loadFilterRules(Context context) {

        File filterDir = new File(context.getExternalFilesDir(""), FILTERS_DIR);

        File[] files = filterDir.listFiles();

        if (files == null) {
            if (!filterDir.exists()) {
                Set<FilterRule> result = new HashSet<>();
                result.add(createExampleFilter());
                return result;
            }
            return null;
        }

        Set<FilterRule> result = new HashSet<>();

        String errors = null;

        for (File file : files) {

            try {

                FileInputStream in = new FileInputStream(file);

                byte[] bytes = new byte[in.available()];
                in.read(bytes);

                Parcel parcel = Parcel.obtain();
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);

                Bundle bundle = Bundle.CREATOR.createFromParcel(parcel);
                parcel.recycle();

                in.close();

                DefaultFilterRule filterRule = new DefaultFilterRule();
                filterRule.restoreFromBundle(bundle);
                result.add(filterRule);

            } catch (IOException e) {
                e.printStackTrace();
                errors = appendError(errors, e.getClass().getSimpleName() + " while reading from a file: " + e.getMessage());
            }

        }

        if (result.isEmpty()) {
            result.add(createExampleFilter());
        }

        //TODO show potential errors!

        return result;
    }

    private static DefaultFilterRule createExampleFilter() {
        DefaultFilterRule rule = new DefaultFilterRule();
        rule.targetAppPackage = "com.supercell.clashofclans";
        rule.query = "Your Village is being raided";
        rule.mode = StringComparisonMode.CONTAINS;
        rule.setEnabled(true);
        return rule;
    }

    private static String appendError(String current, String appendMessage) {
        if (current == null) return appendMessage;
        return current + "\n" + appendMessage;
    }
}