package mycroft.ai.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mycroft.ai.MainActivity;
import mycroft.ai.utils.NetworkUtil;

/**
 * Created by paul on 2016/06/22.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    MainActivity main = null;

    public void setMainActivityHandler(MainActivity main){
        this.main = main;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        int status = NetworkUtil.getConnectivityStatusString(context);
        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                // do something about it.. IDK
            } else {
                // reconnect websocket
                main.connectWebSocket();
            }

        }
    }
}
