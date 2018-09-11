package mycroft.ai.utils

import org.altbeacon.beacon.AltBeacon
import org.altbeacon.beacon.Beacon

import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

import org.altbeacon.beacon.simulator.BeaconSimulator;

/**
 * Created by sarahkraynick on 2017-08-16.
 */
class BeaconSimulator :  BeaconSimulator {
    private var beacons: MutableList<Beacon>? = null

    var USE_SIMULATED_BEACONS = false

    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    /**
     * Creates empty beacons ArrayList.
     */
    init {
        beacons = ArrayList()
    }

    /**
     * Required getter method that is called regularly by the Android Beacon Library.
     * Any beacons returned by this method will appear within your test environment immediately.
     */
    override fun getBeacons(): List<Beacon>? {
        return beacons
    }

    /**
     * Creates simulated beacons all at once.
     */
    fun createBasicSimulatedBeacons() {
        if (USE_SIMULATED_BEACONS) {
            val beacon1 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("1").setRssi(-55).setTxPower(-55).build()
            val beacon2 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("2").setRssi(-55).setTxPower(-55).build()
            val beacon3 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("3").setRssi(-55).setTxPower(-55).build()
            val beacon4 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("4").setRssi(-55).setTxPower(-55).build()
            beacons!!.add(beacon1)
            beacons!!.add(beacon2)
            beacons!!.add(beacon3)
            beacons!!.add(beacon4)
        }
    }


    /**
     * Simulates a new beacon every 10 seconds until it runs out of new ones to add.
     */
    fun createTimedSimulatedBeacons() {
        if (USE_SIMULATED_BEACONS) {
            beacons = ArrayList()
            val beacon1 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("1").setRssi(-55).setTxPower(-55).build()
            val beacon2 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("2").setRssi(-55).setTxPower(-55).build()
            val beacon3 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("3").setRssi(-55).setTxPower(-55).build()
            val beacon4 = AltBeacon.Builder().setId1("DF7E1C79-43E9-44FF-886F-1D1F7DA6997A")
                    .setId2("1").setId3("4").setRssi(-55).setTxPower(-55).build()
            beacons!!.add(beacon1)
            beacons!!.add(beacon2)
            beacons!!.add(beacon3)
            beacons!!.add(beacon4)

            val finalBeacons = ArrayList(beacons!!)

            //Clearing beacons list to prevent all beacons from appearing immediately.
            //These will be added back into the beacons list from finalBeacons later.
            beacons!!.clear()

            scheduleTaskExecutor = Executors.newScheduledThreadPool(5)

            // This schedules an beacon to appear every 10 seconds:
            scheduleTaskExecutor!!.scheduleAtFixedRate({
                try {
                    //putting a single beacon back into the beacons list.
                    if (finalBeacons.size > beacons!!.size)
                        beacons!!.add(finalBeacons[beacons!!.size])
                    else
                        scheduleTaskExecutor!!.shutdown()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 0, 10, TimeUnit.SECONDS)
        }
    }

    companion object {
        protected val TAG = "BeaconSimulator"
    }
}
