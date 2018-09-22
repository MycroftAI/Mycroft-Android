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

package mycroft.ai.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

import mycroft.ai.MainActivity
import mycroft.ai.utils.NetworkUtil

/**
 * Simple class to detect changes in network connectivity.
 *
 *
 * It should trigger connection and disconnection actions
 * on the appropriate handler, which for now is [MainActivity].
 *
 *
 * @see .setMainActivityHandler
 * @author Paul Scott
 */
class NetworkChangeReceiver : BroadcastReceiver() {

    private var main: MainActivity? = null

    fun setMainActivityHandler(main: MainActivity?) {
        this.main = main
    }

    override fun onReceive(context: Context, intent: Intent) {
        val status = NetworkUtil.getConnectivityStatusString(context)
        if ("android.net.conn.CONNECTIVITY_CHANGE" != intent.action) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Toast.makeText(main,"You are not connected to network",Toast.LENGTH_SHORT).show()
            } else if (main != null) {
                // reconnect websocket
                if (main!!.mWebSocketClient == null || main!!.mWebSocketClient!!.connection.isClosed) {
                    main!!.connectWebSocket()
                }
            }
        }
    }
}
