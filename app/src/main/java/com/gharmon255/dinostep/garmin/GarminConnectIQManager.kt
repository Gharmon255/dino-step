package com.gharmon255.dinostep.garmin

import android.app.Activity
import android.content.Context
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQDevice
import com.garmin.android.connectiq.exception.InvalidStateException
import com.garmin.android.connectiq.exception.ServiceUnavailableException

/**
 * Owns the Connect IQ Mobile SDK singleton for the app process.
 * Must be bound from a visible [Activity] — never from [android.app.Application].
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

    var hasBindAttempted: Boolean = false
        private set

    /**
     * Call from [com.gharmon255.dinostep.MainActivity.onCreate] before game publish.
     * [autoUi] is false so the SDK never shows dialogs (prevents BadTokenException).
     */
    fun bindActivity(activity: Activity) {
        if (hasBindAttempted) {
            return
        }
        hasBindAttempted = true
        try {
            val instance = ConnectIQ.getInstance(activity, ConnectIQ.IQConnectType.WIRELESS)
            connectIQ = instance
            instance.initialize(activity, false, object : ConnectIQ.ConnectIQListener {
                override fun onSdkReady() {
                    isSdkReady = true
                    lastInitError = null
                    Log.i(TAG, "Connect IQ SDK ready")
                }

                override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
                    isSdkReady = false
                    lastInitError = status.name
                    Log.w(TAG, "Connect IQ SDK init error: ${status.name}")
                }

                override fun onSdkShutDown() {
                    isSdkReady = false
                    Log.i(TAG, "Connect IQ SDK shut down")
                }
            })
        } catch (error: InvalidStateException) {
            connectIQ = null
            isSdkReady = false
            lastInitError = error.message ?: "invalid_state"
            Log.e(TAG, "Connect IQ init invalid state", error)
        } catch (error: ServiceUnavailableException) {
            connectIQ = null
            isSdkReady = false
            lastInitError = "garmin_connect_unavailable"
            Log.w(TAG, "Garmin Connect Mobile unavailable — phone app continues without Garmin", error)
        } catch (error: Exception) {
            connectIQ = null
            isSdkReady = false
            lastInitError = error.message ?: error.javaClass.simpleName
            Log.e(TAG, "Connect IQ init failed — phone app continues without Garmin", error)
        }
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
            hasBindAttempted = false
        }
    }

    companion object {
        private const val TAG = "DinoStepGarminSdk"
    }
}
