package com.flipdesk.core.platform

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SystemStatusSnapshot(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val wifiConnected: Boolean = false,
    val wifiEnabled: Boolean = false,
    val cellularConnected: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val freeStorageGb: Float = 0f,
    val totalStorageGb: Float = 0f,
    val availableRamGb: Float = 0f,
    val totalRamGb: Float = 0f
)

@Singleton
class SystemStatusProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun currentSnapshot(): SystemStatusSnapshot {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val wifiConnected = runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }.getOrDefault(false)
        val cellularConnected = runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        }.getOrDefault(false)

        val wifiEnabled = runCatching {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wm?.isWifiEnabled == true
        }.getOrDefault(false)

        val btEnabled = runCatching {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter?.isEnabled == true
        }.getOrDefault(false)

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalStorage = statFs.totalBytes / (1024f * 1024f * 1024f)
        val freeStorage = statFs.availableBytes / (1024f * 1024f * 1024f)

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val totalRam = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availRam = memInfo.availMem / (1024f * 1024f * 1024f)

        return SystemStatusSnapshot(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            wifiConnected = wifiConnected,
            wifiEnabled = wifiEnabled,
            cellularConnected = cellularConnected,
            bluetoothEnabled = btEnabled,
            freeStorageGb = freeStorage,
            totalStorageGb = totalStorage,
            availableRamGb = availRam,
            totalRamGb = totalRam
        )
    }

    fun observeSystemStatus(): Flow<SystemStatusSnapshot> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(currentSnapshot())
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        trySend(currentSnapshot())
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }
}
