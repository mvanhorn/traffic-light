package com.leekleak.shizukuintegration

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.telephony.SubscriptionInfo
import rikka.shizuku.Shizuku
import timber.log.Timber

class ShizukuHelper(
    applicationId: String,
    debuggable: Boolean,
    version: Int,
    onConnectionCallback: () -> Unit
) {
    private var binderMine: ITrafficLightShizukuService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder?) {
            if (binder != null && binder.pingBinder()) {
                binderMine = ITrafficLightShizukuService.Stub.asInterface(binder)
                onConnectionCallback()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            binderMine = null
        }
    }

    private val serviceArgs = Shizuku.UserServiceArgs(
        ComponentName(applicationId, TrafficLightShizukuService::class.java.name)
    )
        .processNameSuffix("traffic_light_shizuku_service")
        .debuggable(debuggable)
        .version(version)

    fun bind() {
        Shizuku.bindUserService(serviceArgs, connection)
    }

    fun unbind() {
        if (binderMine != null) {
            Shizuku.unbindUserService(serviceArgs, connection, true)
        }
    }

    fun getSubscriptionInfos(): List<SubscriptionInfo> {
        try {
            return binderMine?.subscriptionInfos ?: emptyList()
        } catch (e: Exception) {
            Timber.w(e)
            return emptyList()
        }
    }

    fun getSubscriberID(subscriptionId: Int): String? {
        try {
            return binderMine?.getSubscriberID(subscriptionId)
        } catch (e: Exception) {
            Timber.w(e)
            return null
        }
    }

    fun shizukuRunning(): Boolean = Shizuku.pingBinder()

    fun shizukuPermission(): Int = Shizuku.checkSelfPermission()
    fun shizukuRequestPermission(): Unit = Shizuku.requestPermission(12199)
}