package com.gharmon255.dinostep.garmin

import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException

/**
 * Owns the Connect IQ Mobile SDK singleton for the app process.
 * Initialized from [com.gharmon255.dinostep.DinoStepApplication].
 */
class GarminConnectIQManager(
    context: Context,
) {
    private val appContext = context.applicationContext
    private var connectIQ: ConnectIQ? = null

    var isSdkReady: Boolean = false
        private set

    var lastInitError: String? = null
        private set

    fun initialize() {
        if (connectIQ != null) {
            return
        }

        val instance = ConnectIQ.getInstance(appContext, ConnectIQ.IQConnectType.WIRELESS)
        connectIQ = instance
        instance.initialize(appContext, true, object : ConnectIQ.ConnectIQListener {
            override fun onSdkReady() {
                isSdkReady = true
                lastInitError = null
                Log.i(TAG, "Connect IQ SDK ready")
            }

            override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
                isSdkReady = false
                lastInitError = status.name
                Log.e(TAG, "Connect IQ SDK init error: ${status.name}")
            }

            override fun onSdkShutDown() {
                isSdkReady = false
                Log.i(TAG, "Connect IQ SDK shut down")
            }
        })
    }

    fun connectedDevices(): List<IQDevice> {
        val iq = connectIQ ?: return emptyList()
        if (!isSdkReady) {
            return emptyList()
        }

        return try {
            (iq.knownDevices ?: emptyList())
                .onEach { device -> device.status = iq.getDeviceStatus(device) }
                .filter { it.status == IQDevice.IQDeviceStatus.CONNECTED }
        } catch (error: InvalidStateException) {
            Log.e(TAG, "ConnectIQ invalid state while listing devices", error)
            emptyList()
        } catch (error: ServiceUnavailableException) {
            Log.e(TAG, "Garmin Connect Mobile unavailable", error)
            emptyList()
        }
    }

    fun requireConnectIQ(): ConnectIQ {
        return connectIQ ?: error("ConnectIQ not initialized")
    }

    fun shutdown() {
        val iq = connectIQ ?: return
        try {
            iq.unregisterAllForEvents()
            iq.shutdown(appContext)
        } catch (error: InvalidStateException) {
            Log.w(TAG, "ConnectIQ already shut down", error)
        } finally {
            connectIQ = null
            isSdkReady = false
        }
    }

    companion object {
        private const val TAG = "DinoStepGarminSdk"
    }
}
