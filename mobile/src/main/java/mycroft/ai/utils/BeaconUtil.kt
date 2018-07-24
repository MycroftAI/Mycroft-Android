package mycroft.ai.utils

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.util.Log

import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter

import java.util.Arrays

/**
 * Created by sarahkraynick on 2017-07-29.
 */
class BeaconUtil(internal var context: Context) {

    fun broadcastAsBeacon() {
        val beacon = Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(*arrayOf(0L))) // Remove this for beacon layouts without d: fields
                .build()
        // Change the layout below for other beacon types
        val beaconParser = BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")
        val beaconTransmitter = BeaconTransmitter(context, beaconParser)
        beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {

            override fun onStartFailure(errorCode: Int) {
                Log.e("Class", "Advertisement start failed with code: $errorCode")
            }

            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.i("class", "Advertisement start succeeded.")
            }
        })
    }
}
