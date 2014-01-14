/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnirom.omnigears.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuickSettingsUtil {
    private static final String TAG = "QuickSettingsUtil";

    public static final Map<String, TileInfo> TILES;

    private static final Map<String, TileInfo> ENABLED_TILES = new HashMap<String, TileInfo>();
    private static final Map<String, TileInfo> DISABLED_TILES = new HashMap<String, TileInfo>();

    public enum Tile {
        USER,
        BRIGHTNESS,
        SETTINGS,
        WIFI,
        RSSI,
        BLUETOOTH,
        VOLUME,
        BATTERY,
        ROTATION,
        IMMERSIVE,
        LOCATION,
        AIRPLANE,
        QUITEHOUR,
        SLEEP,
        SYNC,
        USBMODE,
        TORCH
    }

    public static ArrayList<String> TILES_DEFAULT = new ArrayList<String>();

    public static final String DELIMITER = ";";

    public static final String DEFAULT_TILES = Tile.USER + DELIMITER + Tile.BRIGHTNESS
        + DELIMITER + Tile.SETTINGS + DELIMITER + Tile.WIFI + DELIMITER + Tile.TORCH
        + DELIMITER + Tile.RSSI + DELIMITER + Tile.BLUETOOTH + DELIMITER + Tile.VOLUME
        + DELIMITER + Tile.BATTERY + DELIMITER + Tile.ROTATION+ DELIMITER + Tile.IMMERSIVE
        + DELIMITER + Tile.LOCATION + DELIMITER + Tile.AIRPLANE + DELIMITER + Tile.QUITEHOUR
        + DELIMITER + Tile.USBMODE + DELIMITER + Tile.SLEEP + DELIMITER + Tile.SYNC;

    static {
           TILES_DEFAULT.add(Tile.USER.toString());
           TILES_DEFAULT.add(Tile.BRIGHTNESS.toString());
           TILES_DEFAULT.add(Tile.SETTINGS.toString());
           TILES_DEFAULT.add(Tile.WIFI.toString());
           TILES_DEFAULT.add(Tile.RSSI.toString());
           TILES_DEFAULT.add(Tile.BLUETOOTH.toString());
           TILES_DEFAULT.add(Tile.VOLUME.toString());
           TILES_DEFAULT.add(Tile.BATTERY.toString());
           TILES_DEFAULT.add(Tile.ROTATION.toString());
           TILES_DEFAULT.add(Tile.IMMERSIVE.toString());
           TILES_DEFAULT.add(Tile.LOCATION.toString());
           TILES_DEFAULT.add(Tile.AIRPLANE.toString());
           TILES_DEFAULT.add(Tile.QUITEHOUR.toString());
           TILES_DEFAULT.add(Tile.SLEEP.toString());
           TILES_DEFAULT.add(Tile.SYNC.toString());
           TILES_DEFAULT.add(Tile.USBMODE.toString());
           TILES_DEFAULT.add(Tile.TORCH.toString());
    }

    static {
        TILES = Collections.unmodifiableMap(ENABLED_TILES);
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.USER.toString(), R.string.title_tile_user,
                "com.android.systemui:drawable/ic_qs_default_user"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.BRIGHTNESS.toString(), R.string.title_tile_brightness,
                "com.android.systemui:drawable/ic_qs_brightness_auto_off"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.SETTINGS.toString(), R.string.title_tile_settings,
                "com.android.systemui:drawable/ic_qs_settings"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.WIFI.toString(), R.string.title_tile_wifi,
                "com.android.systemui:drawable/ic_qs_wifi_full_4"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.RSSI.toString(), R.string.title_tile_mobiledata,
                "com.android.systemui:drawable/ic_qs_signal_full_4"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.BLUETOOTH.toString(), R.string.title_tile_bluetooth,
                "com.android.systemui:drawable/ic_qs_bluetooth_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.VOLUME.toString(), R.string.title_tile_volume,
                "com.android.systemui:drawable/ic_qs_volume"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.BATTERY.toString(), R.string.title_tile_battery,
                "com.android.systemui:drawable/ic_qs_battery_neutral"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.ROTATION.toString(), R.string.title_tile_autorotate,
                "com.android.systemui:drawable/ic_qs_auto_rotate"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.IMMERSIVE.toString(), R.string.title_tile_immersive,
                "com.android.systemui:drawable/ic_qs_immersive_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.LOCATION.toString(), R.string.title_tile_location,
                "com.android.systemui:drawable/ic_qs_location_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.AIRPLANE.toString(), R.string.title_tile_airplane,
                "com.android.systemui:drawable/ic_qs_airplane_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.QUITEHOUR.toString(), R.string.title_tile_quiet_hours,
                "com.android.systemui:drawable/ic_qs_quiet_hours_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.SLEEP.toString(), R.string.title_tile_sleep,
                "com.android.systemui:drawable/ic_qs_sleep"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.SYNC.toString(), R.string.title_tile_sync,
                "com.android.systemui:drawable/ic_qs_sync_on"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.USBMODE.toString(), R.string.title_tile_usbmode,
                "com.android.systemui:drawable/ic_qs_usb_device"));
        registerTile(new QuickSettingsUtil.TileInfo(
                Tile.TORCH.toString(), R.string.title_tile_torch,
                "com.android.systemui:drawable/ic_qs_torch_on"));
    }

    private static void registerTile(QuickSettingsUtil.TileInfo info) {
        ENABLED_TILES.put(info.getId(), info);
    }

    private static void removeTile(String id) {
        ENABLED_TILES.remove(id);
        DISABLED_TILES.remove(id);
        TILES_DEFAULT.remove(id);
    }

    private static void disableTile(String id) {
        if (ENABLED_TILES.containsKey(id)) {
            DISABLED_TILES.put(id, ENABLED_TILES.remove(id));
        }
    }

    private static void enableTile(String id) {
        if (DISABLED_TILES.containsKey(id)) {
            ENABLED_TILES.put(id, DISABLED_TILES.remove(id));
        }
    }

    private static synchronized void removeUnsupportedTiles(Context context) {
        ConnectivityManager mCM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Don't show mobile data options if not supported
        if (!mCM.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
            removeTile(Tile.RSSI.toString());
        }

        // Don't show the bluetooth options if not supported
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            removeTile(Tile.BLUETOOTH.toString());
        }
    }

    public static synchronized void updateAvailableTiles(Context context) {
        removeUnsupportedTiles(context);
    }

    public static boolean isTileAvailable(String id) {
        return ENABLED_TILES.containsKey(id);
    }

    public static String getCurrentTiles(Context context) {
        String tiles = Settings.System.getString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS_RIBBON_TILES);
        if (tiles == null) {
            tiles = getDefaultTiles(context);
        }
        return tiles;
    }

    public static void saveCurrentTiles(Context context, String tiles) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS_RIBBON_TILES, tiles);
    }

    public static void resetTiles(Context context) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.QUICK_SETTINGS_RIBBON_TILES, DEFAULT_TILES);
    }

    public static String mergeInNewTileString(String oldString, String newString) {
        ArrayList<String> oldList = getTileListFromString(oldString);
        ArrayList<String> newList = getTileListFromString(newString);
        ArrayList<String> mergedList = new ArrayList<String>();

        // add any items from oldlist that are in new list
        for (String tile : oldList) {
            if (newList.contains(tile)) {
                mergedList.add(tile);
            }
        }

        // append anything in newlist that isn't already in the merged list to
        // the end of the list
        for (String tile : newList) {
            if (!mergedList.contains(tile)) {
                mergedList.add(tile);
            }
        }

        // return merged list
        return getTileStringFromList(mergedList);
    }

    public static ArrayList<String> getTileListFromString(String tiles) {
        return new ArrayList<String>(Arrays.asList(tiles.split(DELIMITER)));
    }

    public static String getTileStringFromList(ArrayList<String> tiles) {
        if (tiles == null || tiles.size() <= 0) {
            return "";
        } else {
            String s = tiles.get(0);
            for (int i = 1; i < tiles.size(); i++) {
                s += DELIMITER + tiles.get(i);
            }
            return s;
        }
    }

    public static String getDefaultTiles(Context context) {
        removeUnsupportedTiles(context);
        return TextUtils.join(DELIMITER, TILES_DEFAULT);
    }

    public static class TileInfo {
        private String mId;
        private int mTitleResId;
        private String mIcon;

        public TileInfo(String id, int titleResId, String icon) {
            mId = id;
            mTitleResId = titleResId;
            mIcon = icon;
        }

        public String getId() {
            return mId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }

        public String getIcon() {
            return mIcon;
        }
    }
}
