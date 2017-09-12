package mycroft.ai;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Created by sarahkraynick on 2017-07-29.
 */
public class MycroftApplication extends Application implements BootstrapNotifier {

    private static final String TAG = "MycroftApplication";
    private static Context context;
    private BeaconActivity beaconActivity;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private BeaconManager beaconManager;

    public void onCreate() {
        super.onCreate();
        MycroftApplication.context = getApplicationContext();
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        backgroundPowerSaver = new BackgroundPowerSaver(this);
        setBeaconScanSettings(getString(R.string.beacon_layout));
    }

    public static Context getAppContext() {
        return MycroftApplication.context;
    }

    /**
     * Set the beacon settings for the beacon layout. The are mapped in the string array.
     */
    public void setBeaconScanSettings(String beaconLayout) {
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(beaconLayout));
    }

    public void setMonitoringActivity(BeaconActivity beaconActivity) {
        this.beaconActivity = beaconActivity;
    }

    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "did enter region.");
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");

            Intent intent = new Intent(this, BeaconActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;
        } else {
            if (beaconActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                beaconActivity.logToDisplay("I see a beacon again" );
            } else {
                Log.d(TAG, "Sending notification.");
                sendNotification();
            }
        }
    }

    @Override
    public void didExitRegion(Region region) {
        if (beaconActivity != null) {
            beaconActivity.logToDisplay("I no longer see a beacon.");
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        if (beaconActivity != null) {
            beaconActivity.logToDisplay("I have just switched from seeing/not seeing beacons: " + state);
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Mycroft Beacon")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.drawable.common_plus_signin_btn_icon_dark);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, BeaconActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
