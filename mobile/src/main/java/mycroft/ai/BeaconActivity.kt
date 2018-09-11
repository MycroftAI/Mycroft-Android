package mycroft.ai

import android.Manifest
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import org.altbeacon.beacon.BeaconManager

/**
 * Created by sarahkraynick on 2017-08-16.
 */
class BeaconActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beacon)
        verifyBluetooth()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.location_request_title)
                builder.setMessage(R.string.location_request_message)
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            PERMISSION_REQUEST_COARSE_LOCATION)
                }
                builder.show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults.size != 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "fine location permission granted")
                    } else {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(R.string.location_result_title)
                        builder.setMessage(R.string.location_result_message)
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setOnDismissListener { }
                        builder.show()
                    }
                    return
                }
            }
        }
    }

    fun onRangingClicked(view: View) {
        val myIntent = Intent(this, RangingActivity::class.java)
        this.startActivity(myIntent)
    }

    public override fun onResume() {
        super.onResume()
        (this.applicationContext as MycroftApplication).setMonitoringActivity(this)
    }

    public override fun onPause() {
        super.onPause()
        (this.applicationContext as MycroftApplication).setMonitoringActivity(null!!)
    }

    @UiThread
    private fun verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.verify_bluetooth_not_enabled_title)
                builder.setMessage(R.string.verify_bluetooth_not_enabled_message)
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { finish() }
                builder.show()
            }
        } catch (e: RuntimeException) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.verify_bluetooth_not_enabled_title)
            builder.setMessage(R.string.verify_bluetooth_not_enabled_message)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener { finish() }
            builder.show()
        }

    }

    fun logToDisplay(line: String) {
        runOnUiThread {
            val editText = this@BeaconActivity
                    .findViewById(R.id.monitoringText) as EditText
            editText.append(line + "\n")
        }
    }

    companion object {
        protected val TAG = "BeaconActivity"
        private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }
}
