package mycroft.ai.utils;

import android.content.Context;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


/**
 * Created by pscot on 6/25/2016.
 */
public class PlayServicesUtil {

    private boolean psInstalled;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "PlayServicesUtil";

    private Context context;

    public PlayServicesUtil(Context context) {
        this.context = context;
        psInstalled = checkPlayServices();
    }

    public boolean isPsInstalled() {
        return psInstalled;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context.getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }
}
