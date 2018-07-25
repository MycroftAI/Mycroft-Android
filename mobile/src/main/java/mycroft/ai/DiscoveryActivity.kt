/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mycroft.ai

import android.content.Context
import android.os.Bundle
import android.util.Log

import java.net.ServerSocket


import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.DiscoveryListener
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import android.annotation.SuppressLint
import android.app.Activity

@SuppressLint("NewApi")
class DiscoveryActivity : Activity() {

    internal lateinit var mDiscoveryListener: DiscoveryListener

    internal var mServiceName: String? = null
    internal var mServiceInfo: NsdServiceInfo? = null
    internal var mServerSocket: ServerSocket? = null
    internal var mLocalPort: Int = 0

    internal lateinit var mNsdManager: NsdManager

    internal val TAG = "ServiceDiscovery"
    internal val SERVICE_TYPE = "_mycroft._tcp"
    internal val SERVICE_NAME = "MycroftAI Websocket"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mNsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager

        initializeDiscoveryListener()

        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)

    }

    fun initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = object : NsdManager.DiscoveryListener {

            //  Called as soon as service discovery begins.
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success: $service")
                if (service.serviceType != SERVICE_TYPE) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Mycroft found!: " + service.serviceType + "  " + service.host + "  " + service.port)
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: $service")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
                mNsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
                mNsdManager.stopServiceDiscovery(this)
            }
        }
    }

    private fun resolveService(service: NsdServiceInfo) {
        mNsdManager.resolveService(service, object : ResolveListener {

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Resolving service...")
                Log.i(TAG, serviceInfo.host.toString())
                Log.i(TAG, "Port: " + serviceInfo.port)
            }

            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                // TODO Auto-generated method stub
                Log.d(TAG, "Service resolve failed!")
            }
        })
    }
}