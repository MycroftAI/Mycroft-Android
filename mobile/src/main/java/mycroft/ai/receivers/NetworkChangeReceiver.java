/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mycroft.ai.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import mycroft.ai.MainActivity;
import mycroft.ai.utils.NetworkUtil;

/**
 * Simple class to detect changes in network connectivity.
 * <p>
 *     It should trigger connection and disconnection actions
 *     on the appropriate handler, which for now is {@link MainActivity}.
 * </p>
 *
 * @see #setMainActivityHandler(MainActivity)
 *
 * @author Paul Scott
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Nullable
    private MainActivity main = null;

    public void setMainActivityHandler(@Nullable MainActivity main){
        this.main = main;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        int status = NetworkUtil.INSTANCE.getConnectivityStatusString(context);
        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.INSTANCE.getNETWORK_STATUS_NOT_CONNECTED()) {
                // do something about it.. IDK
            } else if (main != null) {
                // reconnect websocket
                if (main.mWebSocketClient == null || main.mWebSocketClient.getConnection().isClosed()) {
                    main.connectWebSocket();
                }
            }

        }
    }
}
