package mycroft.ai

import android.os.Bundle
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
import android.widget.EditText

import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region

/**
 * Created by sarahkraynick on 2017-08-16.
 */
class RangingActivity : AppCompatActivity(), BeaconConsumer {
    private val beaconManager = BeaconManager.getInstanceForApplication(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranging)

        beaconManager.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    override fun onPause() {
        super.onPause()
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true)
    }

    override fun onResume() {
        super.onResume()
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.setRangeNotifier { beacons, region ->
            if (beacons.size > 0) {
                //EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                val firstBeacon = beacons.iterator().next()
                logToDisplay("The first beacon "
                        + firstBeacon.toString() + " is about "
                        + firstBeacon.distance + " meters away.")
            }
        }

        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        } catch (e: RemoteException) {
        }

    }

    private fun logToDisplay(line: String) {
        runOnUiThread {
            val editText = this@RangingActivity.findViewById(R.id.rangingText) as EditText
            editText.append(line + "\n")
        }
    }

    companion object {

        protected val TAG = "RangingActivity"
    }
}
