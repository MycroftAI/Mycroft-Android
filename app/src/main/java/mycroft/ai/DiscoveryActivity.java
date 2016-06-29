package mycroft.ai;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import mycroft.ai.utils.NetworkAutoDiscoveryUtil;

public class DiscoveryActivity extends AppCompatActivity {

    private Handler mUpdateHandler;
    private TextView mStatusView;
    public static final String TAG = "Discovery";
    private NetworkAutoDiscoveryUtil mDiscoveryUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        mStatusView = (TextView) findViewById(R.id.status);

        mDiscoveryUtil = new NetworkAutoDiscoveryUtil(this);
        mDiscoveryUtil.initializeNsd();
        // Register service
        mDiscoveryUtil.registerService(8000);
    }

    public void clickDiscover(View v) {
        mDiscoveryUtil.discoverServices();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");

        mDiscoveryUtil = new NetworkAutoDiscoveryUtil(this);
        mDiscoveryUtil.initializeNsd();
        // Register service
        mDiscoveryUtil.registerService(8000);
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (mDiscoveryUtil != null) {
            mDiscoveryUtil.stopDiscovery();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming.");
        super.onResume();
        if (mDiscoveryUtil != null) {
            mDiscoveryUtil.discoverServices();
        }
    }

    // For KitKat and earlier releases, it is necessary to remove the
    // service registration when the application is stopped.  There's
    // no guarantee that the onDestroy() method will be called (we're
    // killable after onStop() returns) and the NSD service won't remove
    // the registration for us if we're killed.
    // In L and later, NsdService will automatically unregister us when
    // our connection goes away when we're killed, so this step is
    // optional (but recommended).
    @Override
    protected void onStop() {
        Log.d(TAG, "Being stopped.");
        mDiscoveryUtil.tearDown();
        mDiscoveryUtil = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Being destroyed.");
        super.onDestroy();
    }
}