/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.omnirom.omnigears.interfacesettings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.quicklaunch.BookmarkPicker;

import java.net.URISyntaxException;

import org.omnirom.omnigears.ui.QuickSettingsUtil;

public class NotificationPanelSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "NotificationPanelSettings";

    private static final String STATUS_BAR_CUSTOM_HEADER = "custom_status_bar_header";
    private static final String QUICKSETTINGS_DYNAMIC = "quicksettings_dynamic_row";
    private static final String QUICKSETTINGS_LINKED = "quicksettings_linked";
    private static final String QUICKSETTINGS_RIBBON = "quicksettings_ribbon";
    private static final String QUICK_RIBBON = "tile_picker";
    private static final String QUICK_SWIPE = "quick_swipe";
    private static final String CLOCK_SHORTCUT = "clock_shortcut";

    private static final int REQUEST_PICK_BOOKMARK = 1;

    private CheckBoxPreference mStatusBarCustomHeader;
    private CheckBoxPreference mQuickSwipe;
    private Preference mClockShortcut;
    private ListPreference mQuickSettingsDynamic;
    private PreferenceScreen mQuickRibbon;
    private SwitchPreference mQuickSettingsRibbon;
    private CheckBoxPreference mQuickSettingsLinked;
    private boolean isLinked;
    private boolean isRibbon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notification_panel_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarCustomHeader = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CUSTOM_HEADER);
        mStatusBarCustomHeader.setChecked(Settings.System.getInt(resolver,
            Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) == 1);
        mStatusBarCustomHeader.setOnPreferenceChangeListener(this);

        mQuickSwipe = (CheckBoxPreference) prefSet.findPreference(QUICK_SWIPE);
        mQuickSwipe.setChecked(Settings.System.getInt(resolver,
            Settings.System.QUICK_SWIPE, 1) == 1);
        mQuickSwipe.setOnPreferenceChangeListener(this);

        mClockShortcut = prefSet.findPreference(CLOCK_SHORTCUT);

        Intent clockShortcutIntent = null;
        String clockShortcutIntentUri = Settings.System.getString(getContentResolver(), Settings.System.CLOCK_SHORTCUT);
        if (clockShortcutIntentUri != null) {
            try {
                clockShortcutIntent = Intent.parseUri(clockShortcutIntentUri, 0);
            } catch (URISyntaxException e) {
                clockShortcutIntent = null;
            }
        }

        if(clockShortcutIntent != null) {
            PackageManager packageManager = getPackageManager();
            ResolveInfo info = packageManager.resolveActivity(clockShortcutIntent, 0);
            if (info != null) {
                mClockShortcut.setSummary(info.loadLabel(packageManager));
            } else {
                mClockShortcut.setSummary(R.string.clock_shortcut_default);
            }
        } else {
            mClockShortcut.setSummary(R.string.clock_shortcut_default);
        }

        mQuickRibbon = (PreferenceScreen) findPreference(QUICK_RIBBON);

        mQuickSettingsRibbon = (SwitchPreference) prefSet.findPreference(QUICKSETTINGS_RIBBON);
        isRibbon = Settings.System.getInt(resolver,
            Settings.System.QUICK_SETTINGS_RIBBON_ENABLED, 1) != 0;
        mQuickSettingsRibbon.setChecked(isRibbon);
        mQuickSettingsRibbon.setOnPreferenceChangeListener(this);

        mQuickSettingsLinked = (CheckBoxPreference) prefSet.findPreference(QUICKSETTINGS_LINKED);
        isLinked = Settings.System.getInt(resolver,
            Settings.System.QUICK_SETTINGS_LINKED_TILES, 0) == 1;
        mQuickSettingsLinked.setChecked(isLinked);
        mQuickSettingsLinked.setOnPreferenceChangeListener(this);

        mQuickRibbon.setEnabled(!isLinked && isRibbon? true : false);

        mQuickSettingsDynamic = (ListPreference) prefSet.findPreference(QUICKSETTINGS_DYNAMIC);
        mQuickSettingsDynamic.setOnPreferenceChangeListener(this);
        int statusQuickSettings = Settings.System.getInt(resolver,
                Settings.System.QUICK_SETTINGS_TILES_ROW, 1);
        mQuickSettingsDynamic.setValue(String.valueOf(statusQuickSettings));
        mQuickSettingsDynamic.setSummary(mQuickSettingsDynamic.getEntry());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_PICK_BOOKMARK) {
            // Returned from the 'pick bookmark for this shortcut' screen
            if (data == null) {
                Log.w(TAG, "Result from bookmark picker does not have an intent.");
                return;
            }
            Settings.System.putString(getContentResolver(), Settings.System.CLOCK_SHORTCUT, data.toUri(0));
            PackageManager packageManager = getPackageManager();
            ResolveInfo info = packageManager.resolveActivity(data, 0);
            if (info != null) {
                mClockShortcut.setSummary(info.loadLabel(packageManager));
            } else {
                mClockShortcut.setSummary(R.string.clock_shortcut_default);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference == mClockShortcut) {
            // Open the screen to pick a bookmark for this shortcut

            Intent intent = new Intent(getActivity(), BookmarkPicker.class);
            startActivityForResult(intent, REQUEST_PICK_BOOKMARK);
            return true;
        }
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        QuickSettingsUtil.updateAvailableTiles(getActivity());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarCustomHeader) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER, value ? 1 : 0);
        } else if (preference == mQuickSwipe) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.QUICK_SWIPE, value ? 1 : 0);
        } else if (preference == mQuickSettingsDynamic) {
            int val = Integer.parseInt((String) objValue);
            int index = mQuickSettingsDynamic.findIndexOfValue((String) objValue);
            Settings.System.putInt(resolver,
                Settings.System.QUICK_SETTINGS_TILES_ROW, val);
            mQuickSettingsDynamic.setSummary(mQuickSettingsDynamic.getEntries()[index]);
        } else if (preference == mQuickSettingsLinked) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.QUICK_SETTINGS_LINKED_TILES, value ? 1 : 0);
            isLinked = Settings.System.getInt(resolver,
                  Settings.System.QUICK_SETTINGS_LINKED_TILES, 0) == 1;
            isRibbon = Settings.System.getInt(resolver,
                  Settings.System.QUICK_SETTINGS_RIBBON_ENABLED, 1) == 1;
            mQuickRibbon.setEnabled(!isLinked && isRibbon? true : false);
        } else if (preference == mQuickSettingsRibbon) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.QUICK_SETTINGS_RIBBON_ENABLED, value ? 1 : 0);
            isLinked = Settings.System.getInt(resolver,
                  Settings.System.QUICK_SETTINGS_LINKED_TILES, 0) == 1;
            isRibbon = Settings.System.getInt(resolver,
                  Settings.System.QUICK_SETTINGS_RIBBON_ENABLED, 1) == 1;
            mQuickRibbon.setEnabled(!isLinked && isRibbon? true : false);
        } else {
            return false;
        }

        return true;
    }
}
