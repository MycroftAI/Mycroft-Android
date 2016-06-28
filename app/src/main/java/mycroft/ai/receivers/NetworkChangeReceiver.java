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
        int status = NetworkUtil.getConnectivityStatusString(context);
        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
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
