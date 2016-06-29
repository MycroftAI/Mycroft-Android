package mycroft.ai.wear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import mycroft.ai.R;

/**
 * Example shell activity which simply broadcasts to our receiver and exits.
 */
public class MycroftStubBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("mycroft.ai.wear.SHOW_NOTIFICATION");
        i.putExtra(MycroftPostNotificationReceiver.CONTENT_KEY, getString(R.string.title));
        sendBroadcast(i);
        finish();
    }
}
