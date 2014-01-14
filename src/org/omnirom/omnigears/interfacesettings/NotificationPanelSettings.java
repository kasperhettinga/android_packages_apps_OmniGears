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

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

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

    private CheckBoxPreference mStatusBarCustomHeader;
    private CheckBoxPreference mQuickSwipe;
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
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
