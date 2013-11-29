/*
 *  Copyright (C) 2014 The OmniROM Project
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.omnirom.omnigears.netmon;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceCategory;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;

import java.util.List;

import org.omnirom.omnigears.R;

public class NetworkMonitorSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "NetworkMonitorSettings";

    private static final String NETWORK_CONNECTIONS_ACTIVE_CATEGORY = "network_connections_established_category";
    private static final String NETWORK_CONNECTIONS_CLOSING_CATEGORY = "network_connections_closewait_category";

    private PreferenceCategory mActiveConnections;
    private PreferenceCategory mClosingConnections;

    private NetworkMonitorModel mModel;

    private NetworkMonitorModel.ConnectionsUpdateListener mUpdater
        = new NetworkMonitorModel.ConnectionsUpdateListener() {
        public void onConnectionsUpdated(final List<NetworkEntry> active, final List<NetworkEntry> closing) {
            updateEntries(active, closing);
            getPreferenceScreen().setEnabled(true);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.network_monitor_settings);

        mModel = new NetworkMonitorModel(mUpdater);

        PreferenceScreen prefSet = getPreferenceScreen();

        mActiveConnections = (PreferenceCategory) prefSet.findPreference(NETWORK_CONNECTIONS_ACTIVE_CATEGORY);
        mClosingConnections = (PreferenceCategory) prefSet.findPreference(NETWORK_CONNECTIONS_CLOSING_CATEGORY);

        mModel.updateConnections();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.netmon, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == R.id.refresh) {
            getPreferenceScreen().setEnabled(false);
            mModel.updateConnections();
        } else {
            // Handle the home button
            return false;
        }

        return true;
    }

    public void updateEntries(final List<NetworkEntry> active, final List<NetworkEntry> closing) {
        mActiveConnections.removeAll();
        mClosingConnections.removeAll();

        for (NetworkEntry entryInfo : active) {
            NetworkEntryPreference entry = new NetworkEntryPreference(getActivity(), entryInfo);
            mActiveConnections.addPreference(entry);
        }

        for (NetworkEntry entryInfo : closing) {
            NetworkEntryPreference entry = new NetworkEntryPreference(getActivity(), entryInfo);
            mClosingConnections.addPreference(entry);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return false;
    }



}
