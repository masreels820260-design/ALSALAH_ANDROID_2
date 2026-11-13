package com.mas.alsalah

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val p = context.getSharedPreferences("prayer_settings", Context.MODE_PRIVATE)
        val lat = p.getFloat("lat", 30.0444f).toDouble()
        val lon = p.getFloat("lon", 31.2357f).toDouble()
        val lead = p.getInt("lead", 15)
        val enabled = BooleanArray(5) { p.getBoolean("enabled_$it", true) }
        AlarmScheduler.schedule(context, lat, lon, lead, enabled)
    }
}
