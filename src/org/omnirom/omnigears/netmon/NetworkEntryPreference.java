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

package org.omnirom.omnigears.netmon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.ImageView;

import org.omnirom.omnigears.R;

public class NetworkEntryPreference extends Preference
    implements OnClickListener, OnLongClickListener {

    private NetworkEntry mEntry;
    private PackageManager mPacMan;

    private ImageView mKillButton;
    private ImageView mPackageIcon;
    private TextView mTitleText;
    private TextView mSummaryText;

    private OnClickListener mButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
           
        }
    };

    public NetworkEntryPreference(Context context, NetworkEntry entry) {
        super(context);
        setLayoutResource(R.layout.preference_network_entry);
        mEntry = entry;
        mPacMan = context.getPackageManager();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        /*
        TODO: Kill connection
        mKillButton = (ImageView) view.findViewById(R.id.button_kill);
        mKillButton.setOnClickListener(mButtonClickListener);
        */

        mPackageIcon = (ImageView) view.findViewById(R.id.entry_package_icon);
        try {
            mPackageIcon.setImageDrawable(mPacMan.getApplicationIcon(mEntry.program));
        } catch (Exception e) {
            // ignore
        }

        mTitleText = (TextView) view.findViewById(R.id.title_net_entry);
        mTitleText.setText(mEntry.foreignAddress);

        mSummaryText = (TextView) view.findViewById(R.id.summary_net_entry);
        mSummaryText.setText(mEntry.localAddress + "\n" + mEntry.program);
    }

    @Override
    public boolean onLongClick(View v) {
        return true;
    }

    @Override
    public void onClick(View v) {
        
    }
}
