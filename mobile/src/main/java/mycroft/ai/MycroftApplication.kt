package mycroft.ai

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log

import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.powersave.BackgroundPowerSaver
import org.altbeacon.beacon.startup.BootstrapNotifier
import org.altbeacon.beacon.startup.RegionBootstrap

/**
 * Created by sarahkraynick on 2017-07-29.
 */
class MycroftApplication : Application(), BootstrapNotifier {
    private var beaconActivity: BeaconActivity? = null
    private var regionBootstrap: RegionBootstrap? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null
    private var haveDetectedBeaconsSinceBoot = false
    private var beaconManager: BeaconManager? = null

    override fun onCreate() {
        super.onCreate()
        MycroftApplication.appContext = applicationContext
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this)

        Log.d(TAG, "setting up background monitoring for beacons and power saving")
        // wake up the app when a beacon is seen
        val region = Region("backgroundRegion", null, null, null)
        regionBootstrap = RegionBootstrap(this, region)

        backgroundPowerSaver = BackgroundPowerSaver(this)
        setBeaconScanSettings(getString(R.string.beacon_layout))
    }

    /**
     * Set the beacon settings for the beacon layout. The are mapped in the string array.
     */
    fun setBeaconScanSettings(beaconLayout: String) {
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout(beaconLayout))
    }

    fun setBeaconActivity(beaconActivity: BeaconActivity) {
        this.beaconActivity = beaconActivity
    }

    fun unsetBeaconActivity() {
        this.beaconActivity = null
    }

    override fun didEnterRegion(arg0: Region) {
        Log.d(TAG, "did enter region.")
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity")

            val intent = Intent(this, BeaconActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            this.startActivity(intent)
            haveDetectedBeaconsSinceBoot = true
        } else {
            if (beaconActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                beaconActivity!!.logToDisplay("I see a beacon again")
            } else {
                Log.d(TAG, "Sending notification.")
                sendNotification()
            }
        }
    }

    override fun didExitRegion(region: Region) {
        if (beaconActivity != null) {
            beaconActivity!!.logToDisplay("I no longer see a beacon.")
        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region) {
        if (beaconActivity != null) {
            beaconActivity!!.logToDisplay("I have just switched from seeing/not seeing beacons: $state")
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.beacon_notification_title))
                .setContentText(getString(R.string.beacon_notification_content))
                .setSmallIcon(R.drawable.ic_mycroft)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, BeaconActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(resultPendingIntent)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    //TODO convert to public member. This will cause problems later.
    companion object {

        private val TAG = "MycroftApplication"
        var appContext: Context? = null
            private set
    }
}
