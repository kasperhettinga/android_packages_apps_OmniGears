/*
 *  Copyright (C) 2013 The OmniROM Project
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

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

class NetworkMonitorModel {

    private static final String TAG = "NetworkMonitorModel";

    private static final int COL_PROTOCOL = 0;
    private static final int COL_LOCAL_ADDR = 3;
    private static final int COL_REMOTE_ADDR = 4;
    private static final int COL_STATE = 5;
    private static final int COL_PIDPROC = 6;

    private static final String IPV6_V4_PREFIX = "::ffff:";
    private static final int IPV6_V4_PREFIX_LENGTH = 7;

    private ConnectionsUpdateListener mListener;
    private List<NetworkEntry> mEntriesActive;
    private List<NetworkEntry> mEntriesClosing;

    public interface ConnectionsUpdateListener {
        public void onConnectionsUpdated(final List<NetworkEntry> active, final List<NetworkEntry> closing);
    }

    public NetworkMonitorModel(ConnectionsUpdateListener listener) {
        mEntriesActive = new ArrayList<NetworkEntry>();
        mEntriesClosing = new ArrayList<NetworkEntry>();
        mListener = listener;
    }

    public void updateConnections() {
        mEntriesActive.clear();
        mEntriesClosing.clear();

        CommandCapture command = new CommandCapture(0, "busybox netstat -nputw | tail -n +3") {
                @Override
                public void output(int id, String line) {
                    String[] rawLineData = line.split(" ");
                    ArrayList<String> lineData = new ArrayList<String>();

                    for (String str : rawLineData) {
                        if (str != null && str.trim().length() > 0) {
                            if (str.contains("netstat:")) {
                                // Sometimes, netstat might derp with the user ID, and return unwanted
                                // data. We just skip that line.
                                return;
                            }

                            lineData.add(str.trim());
                            Log.e("XPLOD", ""+lineData.size()+": " + str.trim());
                        }
                    }

                    String[] procInfo = lineData.get(COL_PIDPROC).split("/");

                    NetworkEntry entry = new NetworkEntry();
                    entry.protocol = lineData.get(COL_PROTOCOL);
                    entry.localAddress = lineData.get(COL_LOCAL_ADDR);
                    entry.foreignAddress = lineData.get(COL_REMOTE_ADDR);
                    entry.state = lineData.get(COL_STATE);

                    // We don't care about connections closing within a second
                    if (entry.state.equals("LAST_ACK")) return;

                    // Try to parse the PID. If some error happens, just ignore it
                    try {
                        entry.pid = Integer.parseInt(procInfo[0]);
                        entry.program = procInfo[1];
                    } catch (Exception e) {
                        // ignore
                    }

                    // Remove unneeded IPv6 prefixes
                    if (entry.localAddress.startsWith(IPV6_V4_PREFIX)) {
                        entry.localAddress = entry.localAddress.substring(IPV6_V4_PREFIX_LENGTH);
                    }

                    if (entry.foreignAddress.startsWith(IPV6_V4_PREFIX)) {
                        entry.foreignAddress = entry.foreignAddress.substring(IPV6_V4_PREFIX_LENGTH);
                    }

                    // Try to lookup hostname instead of IP address
                    try {
                        int portIndex = entry.foreignAddress.lastIndexOf(":");
                        InetAddress addr = InetAddress
                            .getByName(entry.foreignAddress.substring(0, portIndex));
                        String port = entry.foreignAddress.substring(portIndex);

                        entry.foreignAddress = addr.getHostName() + port;
                    } catch (UnknownHostException ex) {
                        // ignore
                    }

                    if (entry.state.equals("ESTABLISHED")) {
                        mEntriesActive.add(entry);
                    } else if (entry.state.equals("CLOSE_WAIT")) {
                        mEntriesClosing.add(entry);
                    }
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    if (mListener != null) {
                        mListener.onConnectionsUpdated(mEntriesActive, mEntriesClosing);
                    }
                }
        };

        try {
            RootTools.getShell(true).add(command);
        } catch (Exception e) {
            Log.e(TAG, "Error while running netstat", e);
        }
    }

}