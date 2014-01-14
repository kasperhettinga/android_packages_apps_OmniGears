/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import org.omnirom.omnigears.ui.QuickSettingsUtil.TileInfo;

import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class QuickSettingsTiles extends Fragment {

    private static final int MENU_RESET = Menu.FIRST;

    private static final int DLG_RESET         = 0;

    private DraggableGridView mDragView;
    private ViewGroup mContainer;
    private LayoutInflater mInflater;
    private Resources mSystemUiResources;
    private TileAdapter mTileAdapter;

    private int mTileTextSize;

    @Override
    public View onCreateView(LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        mDragView = new DraggableGridView(getActivity());
        mContainer = container;
        mContainer.setClipChildren(false);
        mContainer.setClipToPadding(false);
        mInflater = inflater;

        PackageManager pm = getActivity().getPackageManager();
        if (pm != null) {
            try {
                mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                mSystemUiResources = null;
            }
        }
        int panelWidth = getItemFromSystemUi("notification_panel_width", "dimen");
        if (panelWidth > 0) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(panelWidth,
                    FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL);
            mDragView.setLayoutParams(params);
        }
        int cellGap = getItemFromSystemUi("quick_settings_cell_gap", "dimen");
        if (cellGap != 0) {
            mDragView.setCellGap(cellGap);
        }
        int columnCount = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QUICK_SETTINGS_TILES_ROW, 0);
        if (columnCount == 1 || columnCount == 4) {
            columnCount = 4;
        } else {
            columnCount = 3;
        }
        if (columnCount != 0) {
            mDragView.setColumnCount(columnCount);
            mTileTextSize = mDragView.getTileTextSize(columnCount);
        }
        mTileAdapter = new TileAdapter(getActivity());
        return mDragView;
    }

    private int getItemFromSystemUi(String name, String type) {
        if (mSystemUiResources != null) {
            int resId = (int) mSystemUiResources.getIdentifier(name, type, "com.android.systemui");
            if (resId > 0) {
                try {
                    if (type.equals("dimen")) {
                        return (int) mSystemUiResources.getDimension(resId);
                    } else {
                        return mSystemUiResources.getInteger(resId);
                    }
                } catch (NotFoundException e) {
                }
            }
        }
        return 0;
    }

    void genTiles() {
        mDragView.removeAllViews();
        ArrayList<String> tiles = QuickSettingsUtil.getTileListFromString(
                QuickSettingsUtil.getCurrentTiles(getActivity()));
        for (String tileindex : tiles) {
            QuickSettingsUtil.TileInfo tile = QuickSettingsUtil.TILES.get(tileindex);
            if (tile != null) {
                addTile(tile.getTitleResId(), tile.getIcon(), 0, false);
            }
        }
        addTile(R.string.add, null, R.drawable.ic_menu_add_dark, false);
    }

    /**
     * Adds a tile to the dragview
     * @param titleId - string id for tile text in systemui
     * @param iconSysId - resource id for icon in systemui
     * @param iconRegId - resource id for icon in local package
     * @param newTile - whether a new tile is being added by user
     */
    void addTile(int titleId, String iconSysId, int iconRegId, boolean newTile) {
        View tileView = null;
        if (iconRegId != 0) {
            tileView = (View) mInflater.inflate(R.layout.quick_settings_tile_generic, null, false);
            final TextView name = (TextView) tileView.findViewById(R.id.text);
            final ImageView iv = (ImageView) tileView.findViewById(R.id.image);
            name.setText(titleId);
            name.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTileTextSize);
            iv.setImageDrawable(getResources().getDrawable(iconRegId));
        } else {
            final boolean isUserTile =
                    titleId == QuickSettingsUtil.TILES.get(QuickSettingsUtil.Tile.USER.toString()).getTitleResId();
            if (mSystemUiResources != null && iconSysId != null) {
                int resId = mSystemUiResources.getIdentifier(iconSysId, null, null);
                if (resId > 0) {
                    try {
                        Drawable d = mSystemUiResources.getDrawable(resId);
                        tileView = null;
                        if (isUserTile) {
                            tileView = (View) mInflater.inflate(
                                    R.layout.quick_settings_tile_user, null, false);
                            ImageView iv = (ImageView) tileView.findViewById(R.id.user_imageview);
                            TextView tv = (TextView) tileView.findViewById(R.id.tile_textview);
                            tv.setText(titleId);
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTileTextSize);
                            iv.setImageDrawable(d);
                        } else {
                            tileView = (View) mInflater.inflate(
                                    R.layout.quick_settings_tile_generic, null, false);
                            final TextView name = (TextView) tileView.findViewById(R.id.text);
                            final ImageView iv = (ImageView) tileView.findViewById(R.id.image);
                            name.setText(titleId);
                            name.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTileTextSize);
                            iv.setImageDrawable(d);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (tileView != null) {
            mDragView.addView(tileView, newTile
                    ? mDragView.getChildCount() - 1 : mDragView.getChildCount());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        genTiles();
        mDragView.setOnRearrangeListener(new DraggableGridView.OnRearrangeListener() {
            public void onRearrange(int oldIndex, int newIndex) {
                ArrayList<String> tiles = QuickSettingsUtil.getTileListFromString(
                        QuickSettingsUtil.getCurrentTiles(getActivity()));
                String oldTile = tiles.get(oldIndex);
                tiles.remove(oldIndex);
                tiles.add(newIndex, oldTile);
                QuickSettingsUtil.saveCurrentTiles(getActivity(),
                        QuickSettingsUtil.getTileStringFromList(tiles));
            }
            @Override
            public void onDelete(int index) {
                ArrayList<String> tiles = QuickSettingsUtil.getTileListFromString(
                        QuickSettingsUtil.getCurrentTiles(getActivity()));
                tiles.remove(index);
                QuickSettingsUtil.saveCurrentTiles(getActivity(),
                        QuickSettingsUtil.getTileStringFromList(tiles));
            }
        });
        mDragView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ArrayList<String> tiles = QuickSettingsUtil.getTileListFromString(
                        QuickSettingsUtil.getCurrentTiles(getActivity()));
                if (arg2 != mDragView.getChildCount() - 1) {
                    if (arg2 == -1) {
                        return;
                    }
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.tile_choose_title)
                .setAdapter(mTileAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, final int position) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String tiles = QuickSettingsUtil.getCurrentTiles(getActivity());
                                ArrayList<String> curr = new ArrayList<String>();
                                // A blank string indicates tiles are currently disabled
                                // Avoid index being off by one when we add a new tile
                                if (!tiles.equals("")) {
                                    curr = QuickSettingsUtil.getTileListFromString(tiles);
                                }
                                curr.add(mTileAdapter.getTileId(position));
                                QuickSettingsUtil.saveCurrentTiles(getActivity(),
                                         QuickSettingsUtil.getTileStringFromList(curr));
                            }
                        }).start();
                        TileInfo info =
                            QuickSettingsUtil.TILES.get(mTileAdapter.getTileId(position));
                        addTile(info.getTitleResId(), info.getIcon(), 0, true);
                    }
                });
                builder.create().show();
            }
        });

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
            default:
                return false;
        }
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        QuickSettingsTiles getOwner() {
            return (QuickSettingsTiles) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.tiles_reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            QuickSettingsUtil.resetTiles(getActivity());
                            getOwner().genTiles();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    private static class TileAdapter extends ArrayAdapter<String> {
        private static class Entry {
            public final TileInfo tile;
            public final String tileTitle;
            public Entry(TileInfo tile, String tileTitle) {
                this.tile = tile;
                this.tileTitle = tileTitle;
            }
        }

        private Entry[] mTiles;

        public TileAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            mTiles = new Entry[getCount()];
            loadItems(context.getResources());
            sortItems();
        }

        private void loadItems(Resources resources) {
            int index = 0;
            for (TileInfo t : QuickSettingsUtil.TILES.values()) {
                mTiles[index++] = new Entry(t, resources.getString(t.getTitleResId()));
            }
        }

        private void sortItems() {
            final Collator collator = Collator.getInstance();
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            collator.setStrength(Collator.PRIMARY);
            Arrays.sort(mTiles, new Comparator<Entry>() {
                @Override
                public int compare(Entry e1, Entry e2) {
                    return collator.compare(e1.tileTitle, e2.tileTitle);
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            v.setEnabled(isEnabled(position));
            return v;
        }

        @Override
        public int getCount() {
            return QuickSettingsUtil.TILES.size();
        }

        @Override
        public String getItem(int position) {
            return mTiles[position].tileTitle;
        }

        public String getTileId(int position) {
            return mTiles[position].tile.getId();
        }

        @Override
        public boolean isEnabled(int position) {
            String usedTiles = QuickSettingsUtil.getCurrentTiles(
                    getContext());
            return !(usedTiles.contains(mTiles[position].tile.getId()));
        }
    }
}
